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
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import br.com.zup.nimbus.processor.model.Property

internal class UnsupportedFunction private constructor (
    source: String,
    property: Property?,
    parameter: KSValueParameter?,
): AutoDeserializationError(
    "Functions are unsupported in $source. Consider creating a custom deserializer for " +
            "your type.",
    property?.parent ?: parameter?.parent as? KSFunctionDeclaration,
    property?.location ?: parameter?.location,
) {
    constructor(source: String, property: Property): this(source, property, null)
    constructor(source: String, parameter: KSValueParameter): this(source, null, parameter)
}
