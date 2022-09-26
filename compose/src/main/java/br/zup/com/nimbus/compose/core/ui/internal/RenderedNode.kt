package br.zup.com.nimbus.compose.core.ui.internal

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import br.zup.com.nimbus.compose.ComponentData
import br.zup.com.nimbus.compose.Nimbus
import br.zup.com.nimbus.compose.NimbusMode
import br.zup.com.nimbus.compose.NimbusTheme
import br.zup.com.nimbus.compose.ui.getComponent
import com.zup.nimbus.core.tree.ServerDrivenNode

@Composable
internal fun ComponentNotFound(name: String) {
    val nimbus = NimbusTheme.nimbus
    val message = "Couldn't find component with id \"$name\"."
    nimbus.logger.error(message)
    if (nimbus.mode == NimbusMode.Development) Text(message)
}

@Composable
internal fun RenderedNode(
    observableNode: ObservableNode,
    parent: ServerDrivenNode? = null,
) {
    val handler = NimbusTheme.nimbus.uiLibraryManager.getComponent(observableNode.node.component)
    // hack: this state only purpose is to force a recomposition
    val (updateValue, forceUpdate) = remember { mutableStateOf(observableNode.forceUpdate) }

    LaunchedEffect(Unit) {
        observableNode.onChange {
            // hack: the following line just forces the updateValue state to be considered by the
            // compose dependency graph
            updateValue.not()
            forceUpdate(observableNode.forceUpdate)
        }
    }

    handler?.let { componentBuilder ->
        componentBuilder(
            ComponentData(
                node = observableNode.node,
                children = {
                    observableNode.children?.forEach {
                        key(observableNode.node.id) { RenderedNode(it, observableNode.node) }
                    }
                },
                parent = parent,
            )
        )
    } ?: ComponentNotFound(observableNode.node.component)
}
