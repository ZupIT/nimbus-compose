package com.zup.nimbus.processor

import com.google.devtools.ksp.symbol.KSFunctionDeclaration

class FunctionInfo(fn: KSFunctionDeclaration) {
    val name: String
    val parameters: List<ParameterInfo>

    init {
        name = fn.simpleName.asString()
        parameters = fn.parameters.map { ParameterInfo(it, fn) }
    }
}
