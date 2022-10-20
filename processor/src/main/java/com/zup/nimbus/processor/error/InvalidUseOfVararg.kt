package com.zup.nimbus.processor.error

import com.google.devtools.ksp.symbol.KSValueParameter

internal class InvalidUseOfVararg(param: KSValueParameter): AutoDeserializationError(
    "vararg can't be used while auto-deserializing components or actions because " +
            "they rely on a property map, not a property list. Please replace vararg with List " +
            "or create a custom deserializer for your type.",
    param,
)
