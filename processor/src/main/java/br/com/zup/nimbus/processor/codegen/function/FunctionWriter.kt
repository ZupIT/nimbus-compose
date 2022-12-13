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
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import br.com.zup.nimbus.processor.model.FunctionWriterResult
import br.com.zup.nimbus.processor.model.IdentifiableKSType
import br.com.zup.nimbus.processor.model.Property
import br.com.zup.nimbus.processor.model.PropertyCategory

/**
 * Writes the main part of a deserialization function. A deserialization function can be a
 * Component, an Action, an Operation or a class deserializer.
 *
 * This object writes the part of the function that performs the actual deserialization. For
 * instance, if the source function or constructor accepted the parameters `(a: String, b: Int?)`,
 * this will write the code:
 *
 * ```
 * val a = properties.get("a").asString()
 * val b = properties.get("b").asIntOrNull()
 * ```
 */
internal object FunctionWriter {
    const val CONTEXT_REF = "__context"
    const val PROPERTIES_REF = "__properties"

    private fun writeProperty(ctx: FunctionWriterContext) {
        when (ctx.property.category) {
            PropertyCategory.Composable -> Component.write(ctx)
            PropertyCategory.Context -> {} // skip
            PropertyCategory.Root -> RootAnnotated.write(ctx)
            PropertyCategory.Common -> CommonProperty.write(ctx)
        }
    }

    fun write(
        parameters: List<Property>,
        deserializers: List<KSFunctionDeclaration>,
        fnBuilder: FunSpec.Builder,
    ): FunctionWriterResult {
        val typesToImport = mutableSetOf<ClassName>()
        val typesToAutoDeserialize = mutableSetOf<IdentifiableKSType>()
        parameters.forEach {
            writeProperty(FunctionWriterContext(
                property = it,
                typesToImport = typesToImport,
                typesToAutoDeserialize = typesToAutoDeserialize,
                builder = fnBuilder,
                deserializers = deserializers,
            ))
        }
        return FunctionWriterResult(typesToImport, typesToAutoDeserialize, fnBuilder)
    }
}