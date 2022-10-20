package com.zup.nimbus.processor.error

import com.google.devtools.ksp.symbol.KSValueParameter

internal class InvalidUseOfRoot(
    param: KSValueParameter,
    message: String = "@Root can't be used on primitive types, lists, maps, enums or functions.",
): AutoDeserializationError(message, param)
