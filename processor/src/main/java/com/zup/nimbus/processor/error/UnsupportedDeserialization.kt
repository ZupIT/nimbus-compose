package com.zup.nimbus.processor.error

import com.google.devtools.ksp.symbol.KSType
import com.zup.nimbus.processor.model.Property
import com.zup.nimbus.processor.utils.getQualifiedName
import com.zup.nimbus.processor.utils.toLocationString

internal class UnsupportedDeserialization(
    type: KSType,
    reason: String,
    property: Property? = null,
): AutoDeserializationError(
    "Cannot create a deserializer for type $type because $reason. " +
            "Try ignoring this parameter (@Ignore) or creating a custom deserializer for it." +
            "\n\tunsupported class at: ${type.getQualifiedName()}" +
            type.declaration.location.toLocationString(),
    property,
)
