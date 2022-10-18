package com.zup.nimbus.processor.error

import com.zup.nimbus.processor.model.Property

class UnsupportedFunction(source: String, property: Property): DeserializationBuildError(
    "Functions are unsupported in $source. Consider creating a custom deserializer for " +
            "your type.",
    property,
)