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

import br.com.zup.nimbus.processor.ClassNames
import br.com.zup.nimbus.processor.codegen.function.FunctionWriter.CONTEXT_REF

/**
 * Writes the code for deserializing a property annotated with @Composable, i.e. the content of
 * a component (children).
 */
internal object Component {
    fun write(ctx: FunctionWriterContext) {
        ctx.typesToImport.add(ClassNames.Column)
        ctx.typesToImport.add(ClassNames.Composable)
        ctx.builder.addStatement(
            "val %L = %L.component?.children ?: run { @Composable {} }",
            ctx.property.name,
            CONTEXT_REF,
        )
    }
}
