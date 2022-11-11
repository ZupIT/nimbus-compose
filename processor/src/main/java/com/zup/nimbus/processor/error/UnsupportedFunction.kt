package com.zup.nimbus.processor.error

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.zup.nimbus.processor.model.Property

internal class UnsupportedFunction private constructor (
    source: String,
    property: Property?,
    parameter: KSValueParameter?,
): AutoDeserializationError(
    "Functions are unsupported in $source. Consider creating a custom deserializer for " +
            "your type.",
    property?.parent ?: parameter?.parent as? KSFunctionDeclaration,
    property?.location ?: parameter?.location,
) {
    constructor(source: String, property: Property): this(source, property, null)
    constructor(source: String, parameter: KSValueParameter): this(source, null, parameter)
}
