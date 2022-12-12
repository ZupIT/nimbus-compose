/*
 * Copyright 2023 ZUP IT SERVICOS EM TECNOLOGIA E INOVACAO SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.zup.nimbus.processor.codegen.function

import br.com.zup.nimbus.processor.utils.isEnum
import br.com.zup.nimbus.processor.utils.isList
import br.com.zup.nimbus.processor.utils.isMap
import br.com.zup.nimbus.processor.utils.isPrimitive

/**
 * Writes the code for deserializing a common property, i.e. a property that is not annotated with
 * `@Root` or `@Composable` and is not a DeserializationContext.
 */
internal object CommonProperty {
    fun write(ctx: FunctionWriterContext) {
        val deserializer = CustomDeserialized.findDeserializer(ctx)
        val type = ctx.property.type
        when {
            deserializer != null -> CustomDeserialized.write(ctx, deserializer)
            type.isPrimitive() -> PrimitiveType.write(ctx)
            type.isEnum() -> EnumType.write(ctx)
            type.isFunctionType -> EventType.write(ctx)
            type.isList() -> ListMapType.writeList(ctx)
            type.isMap() -> ListMapType.writeMap(ctx)
            else -> AutoDeserialized.write(ctx)
        }
    }
}
