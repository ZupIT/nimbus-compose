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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.com.zup.nimbus.annotation.AutoDeserialize

private fun getColor(colorString: String): Color {
    return Color(android.graphics.Color.parseColor(colorString))
}

@Composable
@AutoDeserialize
fun NimbusContainer(
    backgroundColor: String?,
    padding: Double?,
    content: @Composable () -> Unit,
) {
    var modifier = Modifier.padding((padding ?: 0.0).dp)
    backgroundColor?.let { modifier = modifier.background(color = getColor(it)) }
    Column(modifier = modifier) { content() }
}
