package com.zup.nimbus.processor.error

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.zup.nimbus.processor.model.Property
import com.zup.nimbus.processor.utils.getQualifiedName
import com.zup.nimbus.processor.utils.getSimpleName
import com.zup.nimbus.processor.utils.toLocationString

class IncompatibleCustomDeserializer(property: Property, deserializer: KSFunctionDeclaration):
    DeserializationBuildError(
        message = "The parameter ${property.name} of type ${property.type} " +
                "expects a non-nullable value, but its deserializer may return null." +
                "\n\tdeserializer declared at ${deserializer.returnType?.resolve()?.getQualifiedName()}" +
                deserializer.location.toLocationString(),
        property,
    )
