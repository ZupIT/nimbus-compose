package com.zup.nimbus.processor.codegen

import com.zup.nimbus.processor.ClassNames
import com.zup.nimbus.processor.model.Property
import com.zup.nimbus.processor.utils.getQualifiedName
import com.zup.nimbus.processor.utils.isKnown
import com.zup.nimbus.processor.utils.isList
import com.zup.nimbus.processor.utils.isMap

object FunctionCaller {
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
}