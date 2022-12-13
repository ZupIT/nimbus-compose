/*
 * Copyright 2023 ZUP IT SERVICOS EM TECNOLOGIA E INOVACAO SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.zup.nimbus.processor.codegen

import br.com.zup.nimbus.annotation.Alias
import br.com.zup.nimbus.annotation.Ignore
import br.com.zup.nimbus.annotation.Root
import br.com.zup.nimbus.processor.codegen.function.CustomDeserialized
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import br.com.zup.nimbus.processor.error.RootCycleError
import br.com.zup.nimbus.processor.utils.getAnnotation
import br.com.zup.nimbus.processor.utils.getQualifiedName
import br.com.zup.nimbus.processor.utils.hasAnnotation

/**
 * When a property is annotated with @Root, we need in several occasions to know which properties
 * are expected to be found inside the Root property. Not only this, but, if one of the child
 * properties is also annotated with @Root, we need to combine the properties of both the parent and
 * the child. This analysis is done recursively.
 *
 * Example:
 * ```
 * @AutoDeserialize
 * fun myAction(@Root propertyA: MyClassA) {
 *     // ...
 * }
 *
 * class MyClassA(val propertyB: String, @Root val propertyC: MyClassB, val propertyD: String) {
 *     // ...
 * }
 *
 * class MyClassB(val propertyE: Int, val propertyF: Boolean) {
 *     // ...
 * }
 * ```
 *
 * In the end, we should expect as properties of myAction:
 * propertyB: String, propertyD: String, propertyE: Int, propertyF: Boolean.
 *
 * The same class can be referenced multiple times throughout the annotation processing, and
 * running this discovery every time the class is found can be a huge performance bottleneck. For
 * this reason, we only calculate the root properties of a class once and store the result to be
 * used whenever needed. This object (singleton) is responsible for calculating and storing these
 * results.
 */
object RootPropertyCalculator {
    /**
     * Root properties for classes that have been already calculated.
     */
    private val results = mutableMapOf<String, List<String>>()
    /**
     * Watches for cycles in the usage of @Root. Cycles are invalid and must throw an exception.
     */
    private val cycleWatcher = mutableSetOf<String>()

    private fun getName(param: KSValueParameter) =
        param.getAnnotation<Alias>()?.name ?: param.name?.asString() ?: ""

    private fun calculateAllParamsInFunction(
        fn: KSFunctionDeclaration,
        deserializers: List<KSFunctionDeclaration>,
    ): List<String>  {
        val result = mutableListOf<String>()
        fn.parameters.forEach { param ->
            if (param.hasAnnotation<Root>()) {
                val paramType = param.type.resolve()
                val deserializer = CustomDeserialized.findDeserializer(paramType, deserializers)
                if (deserializer == null) {
                    result.addAll(getRootPropertiesOfParameter(param, deserializers))
                }
            } else if (!param.hasAnnotation<Ignore>()) result.add(getName(param))
        }
        return result.distinct()
    }

    private fun calculateAllParamsInTypeConstructor(
        type: KSType,
        deserializers: List<KSFunctionDeclaration>,
    ) {
        val name = type.getQualifiedName() ?: ""
        // starts processing this type, any reference to it while it hasn't finished indicates a
        // cyclic reference
        cycleWatcher.add(name)
        val declaration = type.declaration
        results[name] = if (declaration is KSClassDeclaration) {
            val constructor = declaration.primaryConstructor
            constructor?.let { calculateAllParamsInFunction(it, deserializers) } ?: emptyList()
        } else emptyList()
        cycleWatcher.remove(name) // finishes processing this type
    }

    /**
     * Similar to `getAllParamsInTypeConstructor` but checks for cyclic references and throws if
     * one is found.
     *
     * Cyclic references must throw in order to avoid generating code that will cause infinite
     * loops. Moreover, it makes no sense to make cyclic references using the @Root annotation,
     * this signifies a modeling error.
     */
    private fun getRootPropertiesOfParameter(
        param: KSValueParameter,
        deserializers: List<KSFunctionDeclaration>,
    ): List<String> {
        val type = param.type.resolve()
        val name = type.getQualifiedName()
        if (cycleWatcher.contains(name)) throw RootCycleError(param)
        return getAllParamsInTypeConstructor(type, deserializers)
    }

    /**
     * Get the names of properties in a class constructor considering the @Root annotation. If
     * a child property is also annotated with @Root, its property names will be collected
     * (recursive).
     */
    fun getAllParamsInTypeConstructor(
        type: KSType,
        deserializers: List<KSFunctionDeclaration>,
    ): List<String> {
        val name = type.getQualifiedName()
        if (!results.containsKey(name)) calculateAllParamsInTypeConstructor(type, deserializers)
        return results[name]!!
    }
}
