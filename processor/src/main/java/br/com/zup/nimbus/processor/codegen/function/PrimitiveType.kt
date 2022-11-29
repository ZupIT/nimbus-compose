package br.com.zup.nimbus.processor.codegen.function

import com.google.devtools.ksp.symbol.KSType
import br.com.zup.nimbus.processor.codegen.function.FunctionWriter.PROPERTIES_REF
import br.com.zup.nimbus.processor.utils.getSimpleName

/**
 * Writes the code for deserializing a primitive type, i.e. String, Boolean, Long, Int, Float,
 * Double or Any.
 */
internal object PrimitiveType {
    fun getCallString(type: KSType, propertiesRef: String): String {
        val nullable = if (type.isMarkedNullable) "OrNull" else ""
        return "$propertiesRef.as${type.getSimpleName()}$nullable()"
    }

    fun write(ctx: FunctionWriterContext) {
        ctx.builder.addStatement(
            "val %L = %L",
            ctx.property.name,
            getCallString(ctx.property.type, ctx.property.getAccessString(PROPERTIES_REF)),
        )
    }
}
