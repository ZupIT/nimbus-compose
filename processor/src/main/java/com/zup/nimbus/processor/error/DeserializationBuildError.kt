package com.zup.nimbus.processor.error

import com.zup.nimbus.processor.model.Property
import com.zup.nimbus.processor.utils.toLocationString

private fun getParameterInfo(property: Property): String {
    return "\n\tparameter at " +
            property.parent.qualifiedName?.asString() +
            property.location.toLocationString()
}

open class DeserializationBuildError(message: String, property: Property? = null):
    Error("$message${if (property == null) "" else getParameterInfo(property)}")
