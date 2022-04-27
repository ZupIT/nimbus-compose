package br.zup.com.nimbus.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import br.zup.com.nimbus.compose.serverdriven.Nimbus
import br.zup.com.nimbus.compose.serverdriven.NimbusTheme.nimbus
import br.zup.com.nimbus.compose.serverdriven.ProvideNimbus
import com.zup.nimbus.core.action.ServerDrivenNavigator
import com.zup.nimbus.core.render.ServerDrivenView
import com.zup.nimbus.core.tree.ServerDrivenNode

class MyNavigator : ServerDrivenNavigator {

}

val serverDrivenNavigator = MyNavigator()

@Composable
fun NimbusProvider(json: String, serverDrivenConfig: Nimbus) {
    NimbusProvider(json, serverDrivenConfig, null)
}

@Composable
fun NimbusProvider(json: String, nimbus: Nimbus, ref: Ref<ServerDrivenView?>?) {
    ProvideNimbus(nimbus) {
        NimbusView(json, serverDrivenNavigator, ref)
    }
}

@Composable
fun NimbusView(
    json: String,
    serverDrivenNavigator: ServerDrivenNavigator,
    ref: Ref<ServerDrivenView?>? = null
) {
    val initialTree = nimbus.core.createNodeFromJson(json = json)
    val view = nimbus.core.createView(serverDrivenNavigator)

    val (currentTree, setCurrentTree) = remember { mutableStateOf(view.getCurrentTree()) }

    LaunchedEffect(true) {
        view.onChange {
            setCurrentTree(it)
        }
        if (ref != null) ref.current = view
        view.renderer.paint(initialTree)
    }

    currentTree?.let {
        RenderTree(viewTree = it)
    }
}

@Composable
fun RenderTree(viewTree: ServerDrivenNode) {
    if (!nimbus.components.containsKey(viewTree.component)) {
        throw Error("Component with type ${viewTree.component} is not registered")
    }
    key(viewTree.id) {
        nimbus.components[viewTree.component]!!(element = viewTree, children = {
            viewTree.children?.forEach {
                RenderTree(it)
            }
        })
    }
}
