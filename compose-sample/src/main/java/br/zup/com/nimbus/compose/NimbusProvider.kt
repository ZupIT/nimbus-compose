package br.zup.com.nimbus.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import br.zup.com.nimbus.compose.components.components
import com.zup.nimbus.core.Nimbus
import com.zup.nimbus.core.ServerDrivenConfig
import com.zup.nimbus.core.action.ServerDrivenNavigator
import com.zup.nimbus.core.render.ServerDrivenView
import com.zup.nimbus.core.tree.ServerDrivenNode

class MyNavigator: ServerDrivenNavigator {

}

@Composable
fun NimbusProvider(json: String, serverDrivenConfig: ServerDrivenConfig) {
    NimbusProvider(json, serverDrivenConfig, null)
}

@Composable
fun NimbusProvider(json: String, serverDrivenConfig: ServerDrivenConfig, ref: Ref<ServerDrivenView?>?) {
    //FIXME Nimbus should not be here
    val nimbus = Nimbus(config = serverDrivenConfig)
    val initialTree = nimbus.createNodeFromJson(json = json)
    val view = remember {  nimbus.createView(MyNavigator()) }

    val (currentTree, setCurrentTree) = remember { mutableStateOf(view.getCurrentTree()) }

    LaunchedEffect(true) {
        view.onChange {
            setCurrentTree(it)
        }
        if (ref != null) ref.current = view
        view.renderer.paint(initialTree)
    }

    currentTree?.let {
        renderTree(viewTree = it)
    }
}

@Composable
fun renderTree(viewTree: ServerDrivenNode) {
    if (!components.containsKey(viewTree.component)) {
        throw Error("Component with type ${viewTree.component} is not registered")
    }
    key(viewTree.id) {
        components[viewTree.component]!!(element = viewTree, children = {
            viewTree.children?.forEach {
                renderTree(it)
            }
        })
    }
}
