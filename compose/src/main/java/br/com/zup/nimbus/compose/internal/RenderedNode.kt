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

package br.com.zup.nimbus.compose.internal

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import br.com.zup.nimbus.compose.ComponentData
import br.com.zup.nimbus.compose.Nimbus
import br.com.zup.nimbus.compose.NimbusMode
import br.com.zup.nimbus.compose.ui.getComponent

@Composable
internal fun ComponentNotFound(name: String) {
    val nimbus = Nimbus.instance
    val message = "Could not find any component named \"$name\"."
    nimbus.logger.error(message)
    if (nimbus.mode == NimbusMode.Development) Text(message, color = Color.Red)
}

@Composable
fun RenderedNode(flow: NodeFlow) {
    val state = flow.collectAsState()
    val (node, children) = state.value
    val ui = Nimbus.instance.uiLibraryManager
    val handler = remember(node.component) { ui.getComponent(node.component) }

    DisposableEffect(Unit) {
        onDispose {
            flow.dispose()
        }
    }

    if (handler == null)
        ComponentNotFound(node.component)
    else
        handler(
            ComponentData(
                node = node,
                children = {
                    children?.forEach {
                        key(it.id) { RenderedNode(it) }
                    }
                },
                childrenAsList = { childrenList(children) }
            )
        )
}

private fun childrenList(children: List<NodeFlow>?): List<@Composable () -> Unit> {
    val mutableList = mutableListOf<@Composable () -> Unit>()

    children?.forEach {
        mutableList.add {
            key(it.id) { RenderedNode(it) }
        }
    }

    return mutableList
}
