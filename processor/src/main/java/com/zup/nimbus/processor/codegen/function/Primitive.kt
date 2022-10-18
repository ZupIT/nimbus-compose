package com.zup.nimbus.processor.codegen.function

import com.google.devtools.ksp.symbol.KSType
import com.zup.nimbus.processor.utils.getSimpleName

internal object Primitive {
    fun getCallString(type: KSType, propertiesRef: String): String {
        val nullable = if (type.isMarkedNullable) "OrNull" else ""
        return "$propertiesRef.as${type.getSimpleName()}$nullable()"
    }

    fun write(ctx: FunctionWriterContext) {
        val propertyRef = "properties.get(\"${ctx.property.alias}\")"
        ctx.builder.addStatement(
            "val %L = %L",
            ctx.property.name,
            getCallString(ctx.property.type, propertyRef),
        )
    }
}
