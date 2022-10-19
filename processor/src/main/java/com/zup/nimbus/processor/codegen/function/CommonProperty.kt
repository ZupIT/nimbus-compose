package com.zup.nimbus.processor.codegen.function

import com.zup.nimbus.processor.utils.isEnum
import com.zup.nimbus.processor.utils.isList
import com.zup.nimbus.processor.utils.isMap
import com.zup.nimbus.processor.utils.isPrimitive

internal object CommonProperty {
    fun write(ctx: FunctionWriterContext) {
        val deserializer = CustomDeserialized.findDeserializer(ctx)
        val type = ctx.property.type
        when {
            deserializer != null -> CustomDeserialized.write(ctx, deserializer)
            type.isPrimitive() -> Primitive.write(ctx)
            type.isEnum() -> Enum.write(ctx)
            type.isFunctionType -> Event.write(ctx)
            type.isList() -> ListMap.writeList(ctx)
            type.isMap() -> ListMap.writeMap(ctx)
            else -> AutoDeserialized.write(ctx)
        }
    }
}