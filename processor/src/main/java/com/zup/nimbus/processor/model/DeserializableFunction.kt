package com.zup.nimbus.processor.model

import com.google.devtools.ksp.symbol.KSFunctionDeclaration

internal class DeserializableFunction(
    val declaration: KSFunctionDeclaration,
    val category: FunctionCategory,
)
