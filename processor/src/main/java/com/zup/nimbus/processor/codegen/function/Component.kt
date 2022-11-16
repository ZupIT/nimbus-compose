package com.zup.nimbus.processor.codegen.function

import com.zup.nimbus.processor.ClassNames
import com.zup.nimbus.processor.codegen.function.FunctionWriter.CONTEXT_REF

/**
 * Writes the code for deserializing a property annotated with @Composable, i.e. the content of
 * a component (children).
 */
internal object Component {
    fun write(ctx: FunctionWriterContext) {
        ctx.typesToImport.add(ClassNames.Column)
        ctx.typesToImport.add(ClassNames.Composable)
        ctx.builder.addStatement(
            "val %L = %L.component?.children ?: run { @Composable {} }",
            ctx.property.name,
            CONTEXT_REF,
        )
    }
}
