package com.zup.nimbus.processor.codegen.function

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName
import com.zup.nimbus.processor.codegen.function.FunctionWriter.CONTEXT_REF
import com.zup.nimbus.processor.codegen.function.FunctionWriter.PROPERTIES_REF
import com.zup.nimbus.processor.error.DeserializerPossiblyNull
import com.zup.nimbus.processor.model.Property
import com.zup.nimbus.processor.utils.getQualifiedName
import com.zup.nimbus.processor.utils.hasSameArguments

internal object CustomDeserialized {
    private fun validate(
        property: Property,
        deserializer: KSFunctionDeclaration,
        type: KSType = property.type,
    ) {
        if (!type.isMarkedNullable &&
            deserializer.returnType?.resolve()?.isMarkedNullable == true) {
            throw DeserializerPossiblyNull(property, deserializer)
        }
    }

    fun findDeserializer(
        type: KSType,
        deserializers: List<KSFunctionDeclaration>,
    ): KSFunctionDeclaration? = deserializers.find {
        val returnType = it.returnType?.resolve()
        returnType?.let { rtype ->
            rtype.getQualifiedName() == type.getQualifiedName() && type.hasSameArguments(rtype)
        } ?: false
    }

    fun findDeserializer(ctx: FunctionWriterContext): KSFunctionDeclaration? = findDeserializer(
        ctx.property.type,
        ctx.deserializers,
    )

    fun getCallString(
        ctx: FunctionWriterContext,
        deserializer: KSFunctionDeclaration,
        type: KSType,
        propertiesRef: String = PROPERTIES_REF,
        contextRef: String = CONTEXT_REF,
    ): String {
        validate(ctx.property, deserializer, type)
        val name = deserializer.simpleName.asString()
        ctx.typesToImport.add(
            ClassName(deserializer.packageName.asString(), name)
        )
        val contextParam = if (deserializer.parameters.size == 1) "" else ", $contextRef"
        return "${name}(${propertiesRef}${contextParam})"
    }

    fun getCallString(
        ctx: FunctionWriterContext,
        deserializer: KSFunctionDeclaration,
        propertiesRef: String = PROPERTIES_REF,
        contextRef: String = "context",
    ): String = getCallString(ctx, deserializer, ctx.property.type, propertiesRef, contextRef)

    fun write(ctx: FunctionWriterContext, deserializer: KSFunctionDeclaration) {
        ctx.builder.addStatement(
            "val %L = %L",
            ctx.property.name,
            getCallString(ctx, deserializer, "$PROPERTIES_REF.get(\"${ctx.property.alias}\")"),
        )
        ctx.typesToImport.add(ClassName(
            deserializer.packageName.asString(),
            deserializer.simpleName.asString(),
        ))
    }
}