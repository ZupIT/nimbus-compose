package br.com.zup.nimbus.processor.error

import br.br.com.zup.nimbus.processor.error.AutoDeserializationError
import com.google.devtools.ksp.symbol.KSDeclaration
import br.com.zup.nimbus.processor.utils.toLocationString

internal class InvalidDeserializerParameters(declaration: KSDeclaration): AutoDeserializationError(
    "A deserializer must be a function with either 1 or 2 parameters, where the one is " +
            "the data (AnyServerDrivenData) and the other is the current context " +
            "(DeserializationContext).\n\tdeserializer declared at " +
            "${declaration.qualifiedName?.asString()}${declaration.location.toLocationString()}",
)