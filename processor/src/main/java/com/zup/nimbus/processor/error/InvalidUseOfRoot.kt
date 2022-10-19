package com.zup.nimbus.processor.error

import com.google.devtools.ksp.symbol.KSValueParameter

class InvalidUseOfRoot(param: KSValueParameter): AutoDeserializationError(
    "@Root can't be used on primitive types, lists, maps, enums or functions.",
    param,
)
