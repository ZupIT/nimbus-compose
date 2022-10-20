package com.zup.nimbus.processor.codegen.function

import com.google.devtools.ksp.symbol.KSType
import com.zup.nimbus.processor.codegen.ParameterUtils
import com.zup.nimbus.processor.codegen.function.FunctionWriter.PROPERTIES_REF

internal object AnyType {
    fun getCallString(type: KSType, propertiesRef: String): String {
        val nullable = if (type.isMarkedNullable) "" else " ?: Any()"
        return "$propertiesRef.value$nullable"
    }

    fun write(ctx: FunctionWriterContext) {
        ctx.builder.addStatement(
            "val %L = %L",
            ctx.property.name,
            getCallString(
                ctx.property.type,
                ctx.property.getAccessString(PROPERTIES_REF),
            ),
        )
    }
}
