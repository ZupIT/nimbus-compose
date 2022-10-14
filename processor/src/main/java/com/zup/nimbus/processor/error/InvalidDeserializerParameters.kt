package com.zup.nimbus.processor.error

class InvalidDeserializerParameters: DeserializationBuildError(
    "A deserializer must be a function with either 1 or 2 parameters, where the first is the " +
            "data (AnyServerDrivenData) and the second, if present, is the current scope (Scope)")
