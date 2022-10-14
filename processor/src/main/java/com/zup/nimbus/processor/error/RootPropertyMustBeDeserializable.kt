package com.zup.nimbus.processor.error

class RootPropertyMustBeDeserializable: DeserializationBuildError("A property marked with @Root" +
        "must be deserializable. Please, make sure an automatic deserializer can be created for" +
        "its type or create a custom deserializer.")
