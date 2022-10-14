package com.zup.nimbus.processor.error

class InvalidRootMark: DeserializationBuildError(
    "@Root can't be used on primitive types, lists, maps, enums or functions.",
)
