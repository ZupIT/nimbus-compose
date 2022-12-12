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

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import br.com.zup.nimbus.processor.codegen.RootPropertyCalculator
import br.com.zup.nimbus.processor.codegen.function.FunctionWriter.PROPERTIES_REF
import br.com.zup.nimbus.processor.error.InvalidUseOfRoot

/**
 * Writes the code for deserializing a property annotated with @Root. The main differences from this
 * to a `CommonProperty` is that:
 * 1. The annotated parameter must be a class of non-primitive type.
 * 2. We don't enter the parameter name in `AnyServerDrivenData` before passing it to the deserializer.
 * 3. If the annotated parameter is optional, we first check the `AnyServerDrivenData` for any of its
 * property keys, if none is found, we don't attempt to deserialize it.
 */
internal object RootAnnotated {
    private fun createListOfKeysForDeserializer(
        deserializer: KSFunctionDeclaration?,
        type: KSType,
        deserializers: List<KSFunctionDeclaration>,
    ): String {
        val params = deserializer?.parameters?.mapNotNull { it.name?.asString() }
            ?: RootPropertyCalculator.getAllParamsInTypeConstructor(type, deserializers)

        return params.joinToString(", ") { "\"$it\"" }
    }

    private fun getCallString(
        ctx: FunctionWriterContext,
        deserializer: KSFunctionDeclaration?,
    ): String {
        return if (deserializer == null) {
            AutoDeserialized.getCallString(ctx)
        } else {
            throw InvalidUseOfRoot(
                ctx.property,
                "@Root can't be used on a parameter that is custom deserialized.",
            )
        }
    }

    private fun writeNullable(ctx: FunctionWriterContext, deserializer: KSFunctionDeclaration?) {
        ctx.builder.addCode(
            """
            |val %L = run {
            |  val propertyNames = listOf(%L)
            |  if (%L.hasAnyOfKeys(propertyNames)) %L
            |  else null
            |}
            |
            """.trimMargin(),
            ctx.property.name,
            createListOfKeysForDeserializer(deserializer, ctx.property.type, ctx.deserializers),
            PROPERTIES_REF,
            getCallString(ctx, deserializer),
        )
    }

    private fun writeNonNullable(ctx: FunctionWriterContext, deserializer: KSFunctionDeclaration?) {
        ctx.builder.addStatement("val %L = %L", ctx.property.name, getCallString(ctx, deserializer))
    }

    fun write(ctx: FunctionWriterContext) {
        val deserializer = CustomDeserialized.findDeserializer(ctx)
        if (ctx.property.type.isMarkedNullable) writeNullable(ctx, deserializer)
        else writeNonNullable(ctx, deserializer)
    }
}