package com.zup.nimbus.processor.error

import com.zup.nimbus.processor.model.Property

class InvalidFunction(property: Property): AutoDeserializationError(
    "Invalid function parameter. The Auto deserialization can interpret as events only " +
            "functions that return Unit and accept no arguments or a single argument which " +
            "must be a primitive type, a list of these or a map where the keys are " +
            "strings and the values are any of the aforementioned. To fix this problem you can " +
            "either ignore this parameter (@Ignore) or create a custom deserializer.",
    property,
)
