package com.zup.nimbus.processor.codegen

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import br.com.zup.nimbus.annotation.Alias
import br.com.zup.nimbus.annotation.Ignore
import br.com.zup.nimbus.annotation.Root
import com.zup.nimbus.processor.codegen.function.CustomDeserialized
import com.zup.nimbus.processor.utils.getAnnotation
import com.zup.nimbus.processor.utils.getQualifiedName
import com.zup.nimbus.processor.utils.hasAnnotation

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
                    result.addAll(getAllParamsInTypeConstructor(paramType, deserializers))
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
        // setting this as an empty list immediately prevents an infinite loop when @Root is used
        // in a cyclic manner. `getAllParamsInTypeConstructor` will be called and instead of
        // recursively entering this function, it will use the result `emptyList`.
        results[name] = emptyList()
        val declaration = type.declaration
        if (declaration is KSClassDeclaration) {
            val constructor = declaration.primaryConstructor
            constructor?.let {
                results[name] = calculateAllParamsInFunction(it, deserializers)
            }
        }
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
