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

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import br.com.zup.nimbus.annotation.AutoDeserialize
import br.com.zup.nimbus.annotation.Root

class TextInputEvents(
    val onChange: ((String) -> Unit)?,
    val onFocus: ((String) -> Unit)?,
    val onBlur: ((String) -> Unit)?,
)

enum class TextInputType(val keyboard: KeyboardType) {
    Text(KeyboardType.Text),
    Password(KeyboardType.Password),
    Email(KeyboardType.Email),
    Number(KeyboardType.Number),
}

@Composable
@AutoDeserialize
fun TextInput(
    value: String,
    label: String,
    type: TextInputType? = null,
    enabled: Boolean? = null,
    @Root events: TextInputEvents? = null,
) {
    val modifier = Modifier.onFocusChanged {
        if (it.isFocused) events?.onFocus?.let { it(value) }
        else events?.onBlur?.let { it(value) }
    }

    TextField(
        value = value,
        label = { Text(label) },
        enabled = enabled == true,
        keyboardOptions = KeyboardOptions(keyboardType = (type ?: TextInputType.Text).keyboard),
        onValueChange = { newValue -> events?.onChange?.let { it(newValue) } },
        modifier = modifier,
    )
}
