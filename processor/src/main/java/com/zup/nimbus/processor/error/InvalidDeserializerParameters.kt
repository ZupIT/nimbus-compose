package com.zup.nimbus.processor.error

import com.google.devtools.ksp.symbol.KSDeclaration
import com.zup.nimbus.processor.utils.toLocationString

class InvalidDeserializerParameters(declaration: KSDeclaration): DeserializationBuildError(
    "A deserializer must be a function with either 1 or 2 parameters, where the first is " +
            "the data (AnyServerDrivenData) and the second, if present, is the current context " +
            "(DeserializationContext).\n\tdeserializer declared at " +
            "${declaration.qualifiedName?.asString()}${declaration.location.toLocationString()}",
)
