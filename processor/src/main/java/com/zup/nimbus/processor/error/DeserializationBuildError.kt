package com.zup.nimbus.processor.error

import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.zup.nimbus.processor.model.Property
import com.zup.nimbus.processor.utils.getQualifiedName
import com.zup.nimbus.processor.utils.toLocationString

private fun getClassOfConstructor(fn: KSFunctionDeclaration): String? {
    return if (fn.isConstructor()) fn.returnType?.resolve()?.getQualifiedName()
    else null
}

private fun getQualifiedNameForParameter(property: Property): String {
    return property.parent.qualifiedName?.asString() ?:
    getClassOfConstructor(property.parent) ?:
    property.parent.simpleName.asString()
}

private fun getParameterInfo(property: Property): String {
    return "\n\tparameter at " +
            getQualifiedNameForParameter(property) +
            property.location.toLocationString()
}

open class DeserializationBuildError(message: String, property: Property? = null):
    Error("$message${if (property == null) "" else getParameterInfo(property)}")
