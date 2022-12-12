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
import com.google.devtools.ksp.symbol.KSType
import br.com.zup.nimbus.processor.model.Property
import br.com.zup.nimbus.processor.utils.getQualifiedName
import br.com.zup.nimbus.processor.utils.toLocationString

internal class UnsupportedDeserialization(
    type: KSType,
    reason: String,
    property: Property? = null,
): AutoDeserializationError(
    "Cannot create a deserializer for type $type because $reason. " +
            "Try ignoring this parameter (@Ignore) or creating a custom deserializer for it." +
            "\n\tunsupported class at: ${type.getQualifiedName()}" +
            type.declaration.location.toLocationString(),
    property,
)
