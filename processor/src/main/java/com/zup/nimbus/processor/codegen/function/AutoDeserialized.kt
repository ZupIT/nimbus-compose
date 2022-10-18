package com.zup.nimbus.processor.codegen.function

import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.zup.nimbus.processor.codegen.EntityWriter
import com.zup.nimbus.processor.error.UndeserializableEntity
import com.zup.nimbus.processor.model.IdentifiableKSType
import com.zup.nimbus.processor.model.Property
import com.zup.nimbus.processor.utils.getPackageName
import com.zup.nimbus.processor.utils.getSimpleName

internal object AutoDeserialized {
    private fun validate(property: Property) {
        if (property.type.declaration.modifiers.contains(Modifier.SEALED)) {
            throw UndeserializableEntity(
                property.type,
                "it's a sealed class",
                property,
            )
        }
    }

    private fun writeNullable(
        property: Property,
        deserializerName: String,
        builder: FunSpec.Builder,
    ) {
        builder.addStatement(
            "val %L = if (properties.containsKey(%S)) %L(properties.get(%S), context) else null",
            property.name,
            property.alias,
            deserializerName,
            property.alias,
        )
    }

    private fun writeNonNullable(
        property: Property,
        deserializerName: String,
        builder: FunSpec.Builder,
    ) {
        builder.addStatement(
            "val %L = %L(properties.get(%S), context)",
            property.name,
            deserializerName,
            property.alias,
        )
    }

    fun getCallString(
        ctx: FunctionWriterContext,
        type: KSType = ctx.property.type,
        propertiesRef: String = "properties",
        contextRef: String = "context",
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
