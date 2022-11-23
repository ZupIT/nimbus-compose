package com.zup.nimbus.processor.error

import com.google.devtools.ksp.symbol.KSValueParameter

internal class InvalidUseOfContext(param: KSValueParameter): AutoDeserializationError(
    "Operations should be pure functions, i.e. they shouldn't have side effects and " +
            "should depend only on its parameters. Operations have no context to inject. Please " +
            "ignore the property with type DeserializationContext, remove it or don't use " +
            "auto-deserialization for this specific operation.",
    param,
)
