package com.zup.nimbus.processor.error

class NoQualifiedName(simpleName: String):
    DeserializationBuildError("Can't find the qualified name for $simpleName")