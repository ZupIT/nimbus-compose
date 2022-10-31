package com.zup.nimbus.processor.codegen.function

import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName
import com.zup.nimbus.processor.codegen.function.FunctionWriter.PROPERTIES_REF
import com.zup.nimbus.processor.utils.getPackageName
import com.zup.nimbus.processor.utils.getSimpleName

internal object EnumType {
    fun getCallString(
        ctx: FunctionWriterContext,
        type: KSType = ctx.property.type,
        propertiesRef: String = PROPERTIES_REF,
    ): String {
        ctx.typesToImport.add(
            ClassName(type.getPackageName(), type.getSimpleName())
        )
        val nullable = if (type.isMarkedNullable) "OrNull" else ""
        return "$propertiesRef.asEnum$nullable(${type.getSimpleName()}.values())"
    }

    fun write(ctx: FunctionWriterContext) {
        ctx.builder.addStatement(
            "val %L = %L",
            ctx.property.name,
            getCallString(
                ctx,
                propertiesRef = ctx.property.getAccessString(PROPERTIES_REF),
            ),
        )
    }
}
