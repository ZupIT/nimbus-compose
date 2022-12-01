package br.com.zup.nimbus.compose.internal

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
                childrenAsList = childrenList(children)
            )
        )
}

fun childrenList(children: List<NodeFlow>?): List<@Composable () -> Unit> {
    val mutableList = mutableListOf<@Composable () -> Unit>()

    children?.forEach {
        mutableList.add {
            key(it.id) { RenderedNode(it) }
        }
    }

    return mutableList
}
