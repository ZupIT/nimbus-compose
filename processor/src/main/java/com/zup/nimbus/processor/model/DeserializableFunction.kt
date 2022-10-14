package com.zup.nimbus.processor.model

import com.google.devtools.ksp.symbol.KSFunctionDeclaration

class DeserializableFunction(
    val declaration: KSFunctionDeclaration,
    val category: FunctionCategory,
)
