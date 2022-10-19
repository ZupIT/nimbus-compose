package com.zup.nimbus.processor.codegen.function

import com.squareup.kotlinpoet.ClassName
import com.zup.nimbus.processor.codegen.function.FunctionWriter.PROPERTIES_REF
import com.zup.nimbus.processor.utils.getPackageName
import com.zup.nimbus.processor.utils.getSimpleName

internal object EnumType {
    fun getCallString(ctx: FunctionWriterContext, propertiesRef: String = PROPERTIES_REF): String {
        ctx.typesToImport.add(
            ClassName(ctx.property.type.getPackageName(), ctx.property.type.getSimpleName())
        )
        val alias = "\"${ctx.property.alias}\""
        val type = ctx.property.type.getSimpleName()
        val nullable = if (ctx.property.type.isMarkedNullable) "OrNull" else ""
        return "$propertiesRef.get($alias).asEnum$nullable($type.values())"
    }

    fun write(ctx: FunctionWriterContext) {
        ctx.builder.addStatement("val %L = %L", ctx.property.name, getCallString(ctx))
    }
}
