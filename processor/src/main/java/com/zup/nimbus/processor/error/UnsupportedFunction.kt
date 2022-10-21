package com.zup.nimbus.processor.error

import com.zup.nimbus.processor.model.Property

internal class UnsupportedFunction(source: String, property: Property): AutoDeserializationError(
    "Functions are unsupported in $source. Consider creating a custom deserializer for " +
            "your type.",
    property,
)