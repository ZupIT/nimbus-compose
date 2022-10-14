package com.zup.nimbus.processor.error

class ListTypeError: DeserializationBuildError("A list must have a type (generic argument) in " +
        "order to be deserialized. Consider specifying a type or creating a custom deserializer.")