package com.zup.nimbus.processor.error

import com.google.devtools.ksp.symbol.KSValueParameter

internal class NamelessProperty(param: KSValueParameter): AutoDeserializationError(
    "A deserializable property must be named.",
    param,
)
