package br.zup.com.nimbus.compose.core.ui.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import br.zup.com.nimbus.compose.NimbusTheme
import com.zup.nimbus.core.tree.ServerDrivenNode

@Composable
internal fun NimbusServerDrivenView(
    viewTree: ServerDrivenNode,
    parentViewTree: ServerDrivenNode? = null
) {

    if (!NimbusTheme.nimbus.components.containsKey(viewTree.component)) {
        throw IllegalArgumentException("Component with type ${viewTree.component} is not registered")
    }
    key(viewTree.id) {
        NimbusTheme.nimbus.components[viewTree.component]!!(
            element = viewTree,
            children = {
                viewTree.children?.forEach {
                    NimbusServerDrivenView(it, parentViewTree = viewTree)
                }
            }, parentElement = parentViewTree)
    }
}

