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
import br.com.zup.nimbus.processor.model.IdentifiableKSType
import br.com.zup.nimbus.processor.model.Property

/**
 * A function context stores all the important information for writing the code for the current
 * property under deserialization.
 *
 * This is important so we don't need to pass a lot of parameters from a function to another. This
 * makes the code much simpler.
 */
internal class FunctionWriterContext(
    /**
     * The current property being deserialized
     */
    val property: Property,
    /**
     * The builder for the current function being generated (Kotlin Poet)
     */
    val builder: FunSpec.Builder,
    /**
     * All custom deserializers available. Custom deserializers are functions annotated with
     * `@Deserializer` in the source code.
     */
    val deserializers: List<KSFunctionDeclaration>,
    /**
     * Use this set to add new imports to the current file being generated
     */
    val typesToImport: MutableSet<ClassName>,
    /**
     * Use this set to inform that a new class must be auto-deserialized
     */
    val typesToAutoDeserialize: MutableSet<IdentifiableKSType>,
)
