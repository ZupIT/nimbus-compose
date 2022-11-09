package br.zup.com.nimbus.compose.internal

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import br.zup.com.nimbus.compose.ComponentData
import br.zup.com.nimbus.compose.NimbusMode
import br.zup.com.nimbus.compose.NimbusTheme
import br.zup.com.nimbus.compose.ui.getComponent

@Composable
internal fun ComponentNotFound(name: String) {
    val nimbus = NimbusTheme.nimbus
    val message = "Couldn't find component with id \"$name\"."
    nimbus.logger.error(message)
    if (nimbus.mode == NimbusMode.Development) Text(message)
}

@Composable
fun RenderedNode(flow: NodeFlow) {
    val state = flow.collectAsState()
    val (node, children) = state.value
    val ui = NimbusTheme.nimbus.uiLibraryManager
    val handler = remember(node.component) { ui.getComponent(node.component) }

    DisposableEffect(Unit) {
        onDispose {
            flow.dispose()
        }
    }

    handler?.let { componentBuilder ->
        componentBuilder(
            ComponentData(
                node = node,
                children = {
                    children?.forEach {
                        key(it.id) { RenderedNode(it) }
                    }
                }
            )
        )
    } ?: ComponentNotFound(node.component)
}
