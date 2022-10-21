package com.zup.nimbus.processor.codegen.function

import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.zup.nimbus.processor.codegen.EntityWriter
import com.zup.nimbus.processor.codegen.ParameterUtils
import com.zup.nimbus.processor.codegen.function.FunctionWriter.CONTEXT_REF
import com.zup.nimbus.processor.codegen.function.FunctionWriter.PROPERTIES_REF
import com.zup.nimbus.processor.error.UnsupportedDeserialization
import com.zup.nimbus.processor.model.IdentifiableKSType
import com.zup.nimbus.processor.model.Property
import com.zup.nimbus.processor.utils.getPackageName

internal object AutoDeserialized {
    private fun validate(property: Property) {
        val modifiers = property.type.declaration.modifiers
        val reason = when {
            modifiers.contains(Modifier.SEALED) -> "it's a sealed class"
            modifiers.contains(Modifier.ABSTRACT) -> "it's an abstract class"
            property.type.arguments.isNotEmpty() -> "it accepts type arguments (generics)"
            else -> null
        }
        if (reason != null) {
            throw UnsupportedDeserialization(property.type, reason, property)
        }
    }

    private fun writeNullable(
        property: Property,
        deserializerName: String,
        builder: FunSpec.Builder,
    ) {
        builder.addStatement(
            "val %L = if (%L) %L(%L, %L) else null",
            property.name,
            property.getContainsString(PROPERTIES_REF),
            deserializerName,
            property.getAccessString(PROPERTIES_REF),
            CONTEXT_REF,
        )
    }

    private fun writeNonNullable(
        property: Property,
        deserializerName: String,
        builder: FunSpec.Builder,
    ) {
        builder.addStatement(
            "val %L = %L(%L, %L)",
            property.name,
            deserializerName,
            property.getAccessString(PROPERTIES_REF),
            CONTEXT_REF,
        )
    }

    fun getCallString(
        ctx: FunctionWriterContext,
        type: KSType = ctx.property.type,
        propertiesRef: String = PROPERTIES_REF,
        contextRef: String = CONTEXT_REF,
    ): String {
        val fnName = EntityWriter.createFunctionName(type)
        ctx.typesToImport.add(ClassName(type.getPackageName(), fnName))
        return "$fnName($propertiesRef, $contextRef)"
    }

    fun write(ctx: FunctionWriterContext) {
        val property = ctx.property
        // todo: treat entities with repeated names
        validate(property)
        ctx.typesToAutoDeserialize.add(IdentifiableKSType(property.type))
        val deserializerName = EntityWriter.createFunctionName(property.type)
        ctx.typesToImport.add(ClassName(property.type.getPackageName(), deserializerName))
        if (property.type.isMarkedNullable) writeNullable(property, deserializerName, ctx.builder)
        else writeNonNullable(property, deserializerName, ctx.builder)
    }
}