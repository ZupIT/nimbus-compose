package com.zup.nimbus.processor.codegen

import com.google.devtools.ksp.symbol.KSValueParameter
import com.zup.nimbus.processor.ClassNames
import com.zup.nimbus.processor.annotation.Ignore
import com.zup.nimbus.processor.error.InvalidUseOfIgnore
import com.zup.nimbus.processor.model.Property
import com.zup.nimbus.processor.utils.getQualifiedName
import com.zup.nimbus.processor.utils.hasAnnotation

internal object ParameterUtils {
    private fun assignParameter(property: Property): String {
        val isContext = property.type.getQualifiedName() == ClassNames.DeserializationContext.canonicalName
        val value = if (isContext) "context" else property.name
        val cast = "" /*when {
            property.type.isList() -> " as ${property.type}"
            property.type.isMap() -> " as ${property.type}"
            !property.type.isKnown() && !property.type.isMarkedNullable -> "!!"
            else -> ""
        }*/
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

    fun convertParametersIntoProperties(params: List<KSValueParameter>): List<Property> {
        return params.filter { !shouldIgnore(it) }.map { Property.fromParameter(it) }
    }
}