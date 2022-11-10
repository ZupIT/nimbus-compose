package com.zup.nimbus.processor.codegen.function

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.zup.nimbus.processor.codegen.RootPropertyCalculator
import com.zup.nimbus.processor.codegen.function.FunctionWriter.PROPERTIES_REF

internal object RootAnnotated {
    private fun createListOfKeysForDeserializer(
        deserializer: KSFunctionDeclaration?,
        type: KSType,
        deserializers: List<KSFunctionDeclaration>,
    ): String {
        val params = deserializer?.parameters?.mapNotNull { it.name?.asString() }
            ?: RootPropertyCalculator.getAllParamsInTypeConstructor(type, deserializers)

        return params.joinToString(", ") { "\"$it\"" }
    }

    private fun getCallString(
        ctx: FunctionWriterContext,
        deserializer: KSFunctionDeclaration?,
    ): String {
        return if (deserializer == null) {
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
            createListOfKeysForDeserializer(deserializer, ctx.property.type, ctx.deserializers),
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