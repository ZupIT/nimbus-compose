package br.zup.com.nimbus.compose

import androidx.compose.runtime.*
import br.zup.com.nimbus.compose.components.components
import com.zup.nimbus.core.Nimbus
import com.zup.nimbus.core.ServerDrivenConfig
import com.zup.nimbus.core.action.ServerDrivenNavigator
import com.zup.nimbus.core.render.ServerDrivenView
import com.zup.nimbus.core.tree.ServerDrivenNode

private val config = ServerDrivenConfig(baseUrl = "http://10.0.2.2:8080", platform = "android")
class MyNavigator: ServerDrivenNavigator {

}

@Composable
fun Nimbus(json: String,) {
    Nimbus(json, null)
}

@Composable
fun Nimbus(json: String, ref: Ref<ServerDrivenView?>?) {
    val nimbus = Nimbus(config = config)
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
