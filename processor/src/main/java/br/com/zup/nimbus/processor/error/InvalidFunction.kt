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
import br.com.zup.nimbus.processor.model.Property

internal class InvalidFunction(property: Property): AutoDeserializationError(
    "Invalid function parameter. The Auto deserialization can interpret as events only " +
            "functions that return Unit and accept no arguments or a single argument which " +
            "must be Any, a primitive type, a list of these or a map where the keys are " +
            "strings and the values are any of the aforementioned. To fix this problem you can " +
            "either ignore this parameter (@Ignore) or create a custom deserializer.",
    property,
)
