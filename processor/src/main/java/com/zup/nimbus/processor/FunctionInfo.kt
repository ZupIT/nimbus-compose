package com.zup.nimbus.processor

import com.google.devtools.ksp.symbol.KSFunctionDeclaration

class FunctionInfo(fn: KSFunctionDeclaration) {
    val name: String
    val parameters: List<ParameterInfo>

    init {
        name = fn.simpleName.asString()
        parameters = fn.parameters.filter {
            // fixme: should not rely on SimpleName
            val result = it.annotations.any {
                annotation -> annotation.shortName.asString() == "Ignore"
            }
            if (result && !it.hasDefault) throw IgnoreWithoutDefaultValueException(it, fn)
            !result
        }.map { ParameterInfo(it, fn) }
    }
}
