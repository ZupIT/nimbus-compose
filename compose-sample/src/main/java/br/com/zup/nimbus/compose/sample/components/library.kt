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

package br.com.zup.nimbus.compose.sample.components

import androidx.compose.runtime.Composable
import br.com.zup.nimbus.compose.ui.NimbusComposeUILibrary

val layoutLib = NimbusComposeUILibrary("layout")
    .addComponent("container") @Composable { NimbusContainer(it) }
    .addComponent("column") @Composable { NimbusContainer(it) }
    .addComponent("text") @Composable { NimbusText(it) }


val customLib = NimbusComposeUILibrary("custom")
    .addComponent("text") @Composable { NimbusText(it) }
    .addComponent("textInput") @Composable { TextInput(it) }

val materialLib = NimbusComposeUILibrary("material")
    .addComponent("text") @Composable { NimbusText(it) }
    .addComponent("button") @Composable { NimbusButton(it) }
    .addComponent("textInput") @Composable { TextInput(it) }
    .addOperation("filterNotes") { arguments ->
        val notesByDate = arguments.first() as Map<String, List<Map<String, Any>>>
        val term = arguments[1] as String
        if (term.isBlank()) notesByDate
        else {
            val result = mutableMapOf<String, List<Map<String, Any>>>()
            notesByDate.forEach { entry ->
                val filtered = entry.value.filter {
                    (it["title"] as String).contains(term) || (it["description"] as String).contains(term)
                }
                if (filtered.isNotEmpty()) result[entry.key] = filtered
            }
            result
        }
    }
