package br.zup.com.nimbus.compose.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import br.zup.com.nimbus.compose.NimbusTheme
import com.zup.nimbus.core.tree.ServerDrivenNode

@Composable
internal fun NimbusServerDrivenView(
    viewTree: ServerDrivenNode,
) {

    if (!NimbusTheme.nimbusAppState.config.components.containsKey(viewTree.component)) {
        throw Error("Component with type ${viewTree.component} is not registered")
    }
    key(viewTree.id) {
        NimbusTheme.nimbusAppState.config.components[viewTree.component]!!(
            element = viewTree,
            children = {
                viewTree.children?.forEach {
                    NimbusServerDrivenView(it)
                }
            })
    }
}

