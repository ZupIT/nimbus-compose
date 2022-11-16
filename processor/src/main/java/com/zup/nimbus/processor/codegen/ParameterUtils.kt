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
        return properties.map { assignParameter((it) )}
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
