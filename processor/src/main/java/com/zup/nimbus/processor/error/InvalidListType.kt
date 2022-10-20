package com.zup.nimbus.processor.error

import com.zup.nimbus.processor.model.Property

internal class InvalidListType(property: Property): AutoDeserializationError(
    "A list must have a type (generic argument) in order to be deserialized. Consider " +
            "specifying a type or creating a custom deserializer.",
    property,
)
