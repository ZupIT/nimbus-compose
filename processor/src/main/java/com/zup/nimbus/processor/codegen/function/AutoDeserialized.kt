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
import com.zup.nimbus.processor.utils.getQualifiedName

/**
 * Writes the code for deserializing a property into an instance of an auto-deserialized class, i.e.
 * a class that is used by a Component, Action or Operation annotated with @AutoDeserialized that
 * doesn't have an associated custom deserializer (@Deserializer).
 */
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

    private fun writeNullable(ctx: FunctionWriterContext) {
        ctx.builder.addStatement(
            "val %L = if (%L) %L else null",
            ctx.property.name,
            ctx.property.getContainsString(PROPERTIES_REF),
            getCallString(ctx = ctx, propertiesRef = ctx.property.getAccessString(PROPERTIES_REF)),
        )
    }

    private fun writeNonNullable(ctx: FunctionWriterContext) {
        ctx.builder.addStatement(
            "val %L = %L",
            ctx.property.name,
            getCallString(ctx = ctx, propertiesRef = ctx.property.getAccessString(PROPERTIES_REF)),
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
        ctx.typesToAutoDeserialize.add(IdentifiableKSType(type))
        return "$fnName($propertiesRef, $contextRef)"
    }

    fun write(ctx: FunctionWriterContext) {
        val property = ctx.property
        // todo: treat entities with repeated names
        validate(property)
        if (property.type.isMarkedNullable) writeNullable(ctx)
        else writeNonNullable(ctx)
    }
}
