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

package br.com.zup.nimbus.processor.model

import com.google.devtools.ksp.symbol.KSFunctionDeclaration

/**
 * A function to undergo auto-deserialization. An action handler, component or operation.
 */
internal class DeserializableFunction(
    /**
     * The declaration of the function.
     */
    val declaration: KSFunctionDeclaration,
    /**
     * The category of the function.
     */
    val category: FunctionCategory,
)
