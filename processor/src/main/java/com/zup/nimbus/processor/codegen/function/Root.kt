package com.zup.nimbus.processor.codegen.function

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.zup.nimbus.processor.codegen.function.FunctionWriter.PROPERTIES_REF
import com.zup.nimbus.processor.model.IdentifiableKSType

internal object Root {
    private fun createListOfKeysForDeserializer(
        deserializer: KSFunctionDeclaration?,
        type: KSType,
    ): String {
        val params = if (deserializer != null) {
            deserializer.parameters
        } else {
            val declaration = type.declaration
            if (declaration is KSClassDeclaration) declaration.primaryConstructor?.parameters
            else null
        }
        return params?.joinToString(", ") {
            "\"${it.name?.asString()}\""
        } ?: ""
    }

    private fun getCallString(
        ctx: FunctionWriterContext,
        deserializer: KSFunctionDeclaration?,
    ): String {
        return if (deserializer == null) {
            ctx.typesToAutoDeserialize.add(IdentifiableKSType(ctx.property.type))
            AutoDeserialized.getCallString(ctx)
        } else {
            CustomDeserialized.getCallString(ctx, deserializer)
        }
    }

    private fun writeNullable(ctx: FunctionWriterContext, deserializer: KSFunctionDeclaration?) {
        ctx.builder.addCode(
            """
            |val %L = run {
            |  val propertyNames = listOf(%L)
            |  if (%L.hasAnyOfKeys(propertyNames)) %L
            |  else null
            |}
            |
            """.trimMargin(),
            ctx.property.name,
            createListOfKeysForDeserializer(deserializer, ctx.property.type),
            PROPERTIES_REF,
            getCallString(ctx, deserializer),
        )
    }

    private fun writeNonNullable(ctx: FunctionWriterContext, deserializer: KSFunctionDeclaration?) {
        ctx.builder.addStatement("val %L = %L", ctx.property.name, getCallString(ctx, deserializer))
    }

    fun write(ctx: FunctionWriterContext) {
        val deserializer = CustomDeserialized.findDeserializer(ctx)
        if (ctx.property.type.isMarkedNullable) writeNullable(ctx, deserializer)
        else writeNonNullable(ctx, deserializer)
    }
}