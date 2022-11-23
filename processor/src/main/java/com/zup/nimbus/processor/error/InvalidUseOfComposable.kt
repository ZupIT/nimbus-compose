package com.zup.nimbus.processor.error

import com.google.devtools.ksp.symbol.KSValueParameter

internal class InvalidUseOfComposable(param: KSValueParameter): AutoDeserializationError(
    "@Composable cannot be used for parameters of action handlers or operations. " +
            "They're only acceptable in components.",
    param,
)
