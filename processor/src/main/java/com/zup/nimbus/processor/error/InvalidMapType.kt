package com.zup.nimbus.processor.error

import com.zup.nimbus.processor.model.Property

class InvalidMapType(property: Property): AutoDeserializationError(
    "A map must have strings as its key and any other type as its value in order to be " +
            "deserialized. Consider fixing the type or creating a custom deserializer.",
    property,
)
