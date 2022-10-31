package com.zup.nimbus.processor.codegen

import com.google.devtools.ksp.symbol.KSValueParameter
import com.zup.nimbus.processor.ClassNames
import br.com.zup.nimbus.annotation.Ignore
import com.zup.nimbus.processor.codegen.function.FunctionWriter.CONTEXT_REF
import com.zup.nimbus.processor.error.InvalidUseOfIgnore
import com.zup.nimbus.processor.model.IndexedProperty
import com.zup.nimbus.processor.model.NamedProperty
import com.zup.nimbus.processor.model.Property
import com.zup.nimbus.processor.utils.getQualifiedName
import com.zup.nimbus.processor.utils.hasAnnotation
import com.zup.nimbus.processor.utils.isBoolean
import com.zup.nimbus.processor.utils.isDouble
import com.zup.nimbus.processor.utils.isFloat
import com.zup.nimbus.processor.utils.isInt
import com.zup.nimbus.processor.utils.isLong
import com.zup.nimbus.processor.utils.resolveListType

internal object ParameterUtils {
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

    private fun assignParameter(property: Property): String {
        val isContext = property.type.getQualifiedName() == ClassNames.DeserializationContext.canonicalName
        val value = if (isContext) CONTEXT_REF else property.name
        val cast = if (property is IndexedProperty && property.isVararg) toVarArgArray(property) else ""
        return "${property.name} = $value$cast"
    }

    fun buildParameterAssignments(properties: List<Property>): List<String> {
        return properties.map { assignParameter((it) )}
    }

    private fun shouldIgnore(param: KSValueParameter): Boolean {
        val ignore = param.hasAnnotation<Ignore>()
        if (ignore && !param.hasDefault) throw InvalidUseOfIgnore(param)
        return ignore
    }

    private fun <T>removeIgnoredAndMap(
        params: List<KSValueParameter>,
        transform: (Int, KSValueParameter) -> T
    ): List<T> = params.filter { !shouldIgnore(it) }.mapIndexed(transform)

    fun convertParametersIntoNamedProperties(params: List<KSValueParameter>): List<NamedProperty> {
        return removeIgnoredAndMap(params) { _, param -> NamedProperty.fromParameter(param) }
    }

    fun convertParametersIntoIndexedProperties(
        params: List<KSValueParameter>,
    ): List<IndexedProperty> = removeIgnoredAndMap(params) { index, param ->
        IndexedProperty.fromParameter(param, index)
    }
}
