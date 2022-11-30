package br.com.zup.nimbus.processor.error

import br.br.com.zup.nimbus.processor.error.AutoDeserializationError
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Location
import br.com.zup.nimbus.processor.model.Property

internal class InvalidUseOfRoot private constructor(
    message: String,
    declaration: KSFunctionDeclaration?,
    location: Location?,
): AutoDeserializationError(message, declaration, location) {
    constructor(param: KSValueParameter, message: String = DEFAULT_MESSAGE):
            this(message, param.parent as? KSFunctionDeclaration, param.location)

    constructor(property: Property, message: String = DEFAULT_MESSAGE):
            this(message, property.parent, property.location)

    companion object {
        private const val DEFAULT_MESSAGE = "@Root can't be used on primitive types, lists, " +
                "maps, enums or functions."
    }
}
