package com.zup.nimbus.processor.error

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.zup.nimbus.processor.utils.toLocationString

class InvalidUseOfIgnore(param: KSValueParameter): DeserializationBuildError(
    "Parameters annotated with @Ignore must have default values.${paramString(param)}"
) {
    companion object {
        private fun paramString(param: KSValueParameter): String {
            val parent = param.parent
            val name = if (parent is KSFunctionDeclaration) parent.qualifiedName?.asString() ?: ""
            else ""
            return "\n\tparameter at $name${param.location.toLocationString()}"
        }
    }
}
