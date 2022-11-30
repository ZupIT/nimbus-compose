package br.com.zup.nimbus.processor.error

import br.br.com.zup.nimbus.processor.error.AutoDeserializationError
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import br.com.zup.nimbus.processor.model.Property
import br.com.zup.nimbus.processor.utils.getQualifiedName
import br.com.zup.nimbus.processor.utils.toLocationString

internal class DeserializerPossiblyNull(property: Property, deserializer: KSFunctionDeclaration):
    AutoDeserializationError(
        message = "The parameter ${property.name} of type ${property.type} " +
                "expects a non-nullable value, but its deserializer may return null." +
                "\n\tdeserializer declared at ${deserializer.returnType?.resolve()?.getQualifiedName()}" +
                deserializer.location.toLocationString(),
        property,
    )
