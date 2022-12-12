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

import br.com.zup.nimbus.annotation.Ignore
import br.com.zup.nimbus.processor.ClassNames
import br.com.zup.nimbus.processor.codegen.function.FunctionWriter.CONTEXT_REF
import com.google.devtools.ksp.symbol.KSValueParameter
import br.com.zup.nimbus.processor.error.InvalidUseOfIgnore
import br.com.zup.nimbus.processor.model.IndexedProperty
import br.com.zup.nimbus.processor.model.NamedProperty
import br.com.zup.nimbus.processor.model.Property
import br.com.zup.nimbus.processor.utils.getQualifiedName
import br.com.zup.nimbus.processor.utils.hasAnnotation
import br.com.zup.nimbus.processor.utils.isBoolean
import br.com.zup.nimbus.processor.utils.isDouble
import br.com.zup.nimbus.processor.utils.isFloat
import br.com.zup.nimbus.processor.utils.isInt
import br.com.zup.nimbus.processor.utils.isLong
import br.com.zup.nimbus.processor.utils.resolveListType

/**
 * Set of utilities to deal with function parameters and properties.
 */
internal object ParameterUtils {
    /**
     * Returns the code to transform a vararg property into an Array, which is acceptable when
     * passing a vararg parameter to a function (*array).
     */
    private fun toVarArgArray(property: Property): String {
        val listType = property.type.resolveListType() ?: property.type
        return when {
            listType.isInt() -> ".toIntArray()"
            listType.isDouble() -> ".toDoubleArray()"
            listType.isFloat() -> ".toFloatArray()"
            listType.isLong() -> ".toLongArray()"
            listType.isBoolean() -> ".toBooleanArray()"
            else -> ".toTypedArray()"
        }
    }

    /**
     * Returns the code for a simple assignment in a function call.
     * Example: `propertyA = propertyA``.
     */
    private fun assignParameter(property: Property): String {
        val isContext = property.type.getQualifiedName() == ClassNames.DeserializationContext.canonicalName
        val value = if (isContext) CONTEXT_REF else property.name
        val cast = if (property is IndexedProperty && property.isVararg) toVarArgArray(property) else ""
        return "${property.name} = $value$cast"
    }

    /**
     * Transforms a list of Property into a list of strings that makes the property assignment
     * in a function call.
     */
    fun buildParameterAssignments(properties: List<Property>): List<String> {
        return properties.map { assignParameter((it) ) }
    }

    /**
     * Return true if the property should be ignored (@Ignore)
     */
    private fun shouldIgnore(param: KSValueParameter): Boolean {
        val ignore = param.hasAnnotation<Ignore>()
        if (ignore && !param.hasDefault) throw InvalidUseOfIgnore(param)
        return ignore
    }

    /**
     * Maps the list of parameters removing any parameter that should be ignored.
     */
    private fun <T>removeIgnoredAndMap(
        params: List<KSValueParameter>,
        transform: (Int, KSValueParameter) -> T
    ): List<T> = params.filterNot { shouldIgnore(it) }.mapIndexed(transform)

    /**
     * Converts a list of parameters into a list of NamedProperty.
     */
    fun convertParametersIntoNamedProperties(params: List<KSValueParameter>): List<NamedProperty> {
        return removeIgnoredAndMap(params) { _, param -> NamedProperty.fromParameter(param) }
    }

    /**
     * Converts a list of parameters into a list of IndexedProperty.
     */
    fun convertParametersIntoIndexedProperties(
        params: List<KSValueParameter>,
    ): List<IndexedProperty> = removeIgnoredAndMap(params) { index, param ->
        IndexedProperty.fromParameter(param, index)
    }
}
