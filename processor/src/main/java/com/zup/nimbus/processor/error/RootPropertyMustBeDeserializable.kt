package com.zup.nimbus.processor.error

import com.zup.nimbus.processor.model.Property
import com.zup.nimbus.processor.utils.getSimpleName

class RootPropertyMustBeDeserializable(property: Property): DeserializationBuildError(
    "The property ${property.name} of type ${property.type.getSimpleName()} marked with " +
            "@Root must be deserializable. Please, make sure an automatic deserializer can be " +
            "created for this type or create a custom deserializer.",
    property,
)
