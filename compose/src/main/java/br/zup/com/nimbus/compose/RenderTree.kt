package br.zup.com.nimbus.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import br.zup.com.nimbus.compose.serverdriven.NimbusTheme
import com.zup.nimbus.core.tree.ServerDrivenNode

@Composable
fun RenderTree(viewTree: ServerDrivenNode) {
    if (!NimbusTheme.nimbusAppState.nimbusCompose.components.containsKey(viewTree.component)) {
        throw Error("Component with type ${viewTree.component} is not registered")
    }
    key(viewTree.id) {
        NimbusTheme.nimbusAppState.nimbusCompose.components[viewTree.component]!!(
            element = viewTree,
            children = {
                viewTree.children?.forEach {
                    RenderTree(it)
                }
            })
    }
}
