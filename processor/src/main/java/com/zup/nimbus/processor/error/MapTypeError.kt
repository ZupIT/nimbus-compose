package com.zup.nimbus.processor.error

class MapTypeError: DeserializationBuildError("A map must have strings as its key and any other " +
        "type as its value in order to be deserialized. Consider fixing the type or creating a " +
        "custom deserializer.")
