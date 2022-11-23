package com.zup.nimbus.processor.error

import com.google.devtools.ksp.symbol.KSValueParameter

internal class InvalidUseOfIgnore(param: KSValueParameter): AutoDeserializationError(
    "Parameters annotated with @Ignore must have default values.",
    param,
)
