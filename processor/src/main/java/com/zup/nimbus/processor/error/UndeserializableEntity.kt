package com.zup.nimbus.processor.error

import com.google.devtools.ksp.symbol.KSType
import com.zup.nimbus.processor.utils.getQualifiedName

class UndeserializableEntity(type: KSType, reason: String): DeserializationBuildError("Cannot " +
        "create a deserializer for type ${type.getQualifiedName()} because $reason. Try " +
        "creating a custom deserializer for it.")
