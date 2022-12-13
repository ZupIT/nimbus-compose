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

import com.google.devtools.ksp.symbol.KSType
import br.com.zup.nimbus.processor.codegen.function.FunctionWriter.PROPERTIES_REF
import br.com.zup.nimbus.processor.error.InvalidMapType
import br.com.zup.nimbus.processor.error.UnsupportedFunction
import br.com.zup.nimbus.processor.utils.isEnum
import br.com.zup.nimbus.processor.utils.isList
import br.com.zup.nimbus.processor.utils.isMap
import br.com.zup.nimbus.processor.utils.isPrimitive
import br.com.zup.nimbus.processor.utils.isString
import br.com.zup.nimbus.processor.utils.resolveListType
import br.com.zup.nimbus.processor.utils.resolveMapType

/**
 * Writes the code for deserializing a property of type List<*> or Map<String, *>.
 */
internal object ListMapType {
    private fun getListCall(ctx: FunctionWriterContext, type: KSType, propertiesRef: String): String {
        val nullable = if (type.isMarkedNullable) "OrNull" else ""
        val optional = if (type.isMarkedNullable) "?" else ""
        val typeOfValues = checkNotNull(type.resolveListType()) {
            // this error should be impossible to reach
            "Lists must always have type arguments"
        }
        val mapped = createItemOfType(ctx, typeOfValues, "it")
        return "$propertiesRef.asList${nullable}()${optional}.map { $mapped }"
    }

    private fun getMapCall(ctx: FunctionWriterContext, type: KSType, propertiesRef: String): String {
        val nullable = if (type.isMarkedNullable) "OrNull" else ""
        val optional = if (type.isMarkedNullable) "?" else ""
        val (typeOfKeys, typeOfValues) = type.resolveMapType()
        if (typeOfKeys?.isString() != true || typeOfValues == null) {
            throw InvalidMapType(ctx.property)
        }
        val mapped = createItemOfType(ctx, typeOfValues, "it.value")
        return "$propertiesRef.asMap${nullable}()${optional}.mapValues { $mapped }"
    }

    /**
     * A recursive function to deserialize a List or Map type. This must be recursive because the
     * types of the items in a List or Map may be another List or Map.
     */
    private fun createItemOfType(
        ctx: FunctionWriterContext,
        type: KSType,
        itemRef: String,
    ): String {
        val deserializer = CustomDeserialized.findDeserializer(type, ctx.deserializers)
        return when {
            deserializer != null -> CustomDeserialized.getCallString(ctx, deserializer, type, itemRef)
            type.isPrimitive() -> PrimitiveType.getCallString(type, itemRef)
            type.isEnum() -> EnumType.getCallString(ctx, type, itemRef)
            type.isList() -> getListCall(ctx, type, itemRef)
            type.isMap() -> getMapCall(ctx, type, itemRef)
            type.isFunctionType -> throw UnsupportedFunction("maps or arrays", ctx.property)
            else -> {
                val call = AutoDeserialized.getCallString(ctx, type, itemRef)
                if (type.isMarkedNullable) "if ($itemRef.isNull()) null else $call"
                else call
            }
        }
    }

    fun writeList(ctx: FunctionWriterContext) {
        ctx.builder.addStatement(
            "val %L = %L",
            ctx.property.name,
            getListCall(
                ctx,
                ctx.property.type,
                ctx.property.getAccessString(PROPERTIES_REF),
            )
        )
    }

    fun writeMap(ctx: FunctionWriterContext) {
        ctx.builder.addStatement(
            "val %L = %L",
            ctx.property.name,
            getMapCall(
                ctx,
                ctx.property.type,
                ctx.property.getAccessString(PROPERTIES_REF),
            )
        )
    }
}
