package com.zup.nimbus.processor.error

import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Location
import com.zup.nimbus.processor.model.Property
import com.zup.nimbus.processor.utils.getQualifiedName
import com.zup.nimbus.processor.utils.toLocationString

open class AutoDeserializationError(
    message: String,
    declaration: KSFunctionDeclaration? = null,
    location: Location? = null,
): Error(
    message +
            if (declaration == null || location == null) ""
            else getInfo(declaration, location)
) {
    constructor(message: String, property: Property? = null): this(
        message,
        property?.parent,
        property?.location,
    )

    constructor(message: String, parameter: KSValueParameter?): this(
        message,
        parameter?.parent as? KSFunctionDeclaration,
        parameter?.location,
    )

    companion object {
        private fun getClassOfConstructor(fn: KSFunctionDeclaration): String? {
            return if (fn.isConstructor()) fn.returnType?.resolve()?.getQualifiedName()
            else null
        }

        private fun getQualifiedNameForParameter(declaration: KSFunctionDeclaration): String {
            return declaration.qualifiedName?.asString()
                ?: getClassOfConstructor(declaration)
                ?: declaration.simpleName.asString()
        }

        private fun getInfo(declaration: KSFunctionDeclaration, location: Location): String {
            return "\n\tparameter at " +
                    getQualifiedNameForParameter(declaration) +
                    location.toLocationString()
        }
  }
}
