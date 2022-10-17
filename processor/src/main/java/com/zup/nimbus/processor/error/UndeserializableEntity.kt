package com.zup.nimbus.processor.error

import com.google.devtools.ksp.symbol.KSType
import com.zup.nimbus.processor.model.Property
import com.zup.nimbus.processor.utils.getQualifiedName
import com.zup.nimbus.processor.utils.getSimpleName
import com.zup.nimbus.processor.utils.toLocationString

class UndeserializableEntity(
    type: KSType,
    reason: String,
    property: Property? = null,
): DeserializationBuildError(
    "Cannot create a deserializer for type ${type.getSimpleName()} because " +
            "$reason. Try creating a custom deserializer for it." +
            "\n\tundeserializable class at: ${type.getQualifiedName()}" +
            type.declaration.location.toLocationString(),
    property,
)
