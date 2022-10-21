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
        val access = ctx.property.getAccessString(propertiesRef)
        val nullable = if (type.isMarkedNullable) "OrNull" else ""
        return "$access.asEnum$nullable(${type.getSimpleName()}.values())"
    }

    fun write(ctx: FunctionWriterContext) {
        ctx.builder.addStatement("val %L = %L", ctx.property.name, getCallString(ctx))
    }
}
