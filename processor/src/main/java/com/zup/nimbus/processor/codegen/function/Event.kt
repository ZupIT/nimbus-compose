package com.zup.nimbus.processor.codegen.function

internal object Event {
    fun write(ctx: FunctionWriterContext) {
        ctx.builder.addStatement(
            "val %L = properties.get(%S).asEvent%L()",
            ctx.property.name,
            ctx.property.alias,
            if (ctx.property.type.isMarkedNullable) "OrNull" else "",
        )
    }
}
