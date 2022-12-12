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

package br.com.zup.nimbus.processor.error

import br.br.com.zup.nimbus.processor.error.AutoDeserializationError
import com.google.devtools.ksp.symbol.KSValueParameter

internal class InvalidUseOfContext(param: KSValueParameter): AutoDeserializationError(
    "Operations should be pure functions, i.e. they shouldn't have side effects and " +
            "should depend only on its parameters. Operations have no context to inject. Please " +
            "ignore the property with type DeserializationContext, remove it or don't use " +
            "auto-deserialization for this specific operation.",
    param,
)
