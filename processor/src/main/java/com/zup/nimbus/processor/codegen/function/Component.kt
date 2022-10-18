package com.zup.nimbus.processor.codegen.function

import com.zup.nimbus.processor.ClassNames

internal object Component {
    fun write(ctx: FunctionWriterContext) {
        ctx.typesToImport.add(ClassNames.Column)
        ctx.builder.addStatement(
            "val %L = context.component?.children ?: Column()",
            ctx.property.name,
        )
    }
}
