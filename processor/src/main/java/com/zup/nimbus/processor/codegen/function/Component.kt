package com.zup.nimbus.processor.codegen.function

import com.zup.nimbus.processor.ClassNames
import com.zup.nimbus.processor.codegen.function.FunctionWriter.CONTEXT_REF

internal object Component {
    fun write(ctx: FunctionWriterContext) {
        ctx.typesToImport.add(ClassNames.Column)
        ctx.typesToImport.add(ClassNames.Composable)
        ctx.builder.addStatement(
            "val %L = %L.component?.children ?: @Composable {}",
            ctx.property.name,
            CONTEXT_REF,
        )
    }
}
