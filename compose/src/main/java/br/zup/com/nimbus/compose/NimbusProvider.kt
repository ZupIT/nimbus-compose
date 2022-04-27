package br.zup.com.nimbus.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import br.zup.com.nimbus.compose.serverdriven.NimbusService
import com.zup.nimbus.core.action.ServerDrivenNavigator
import com.zup.nimbus.core.render.ServerDrivenView
import com.zup.nimbus.core.tree.ServerDrivenNode

class MyNavigator: ServerDrivenNavigator {

}
val serverDrivenNavigator = MyNavigator()

@Composable
fun NimbusProvider(json: String, serverDrivenConfig: NimbusService) {
    NimbusProvider(json, serverDrivenConfig, null)
}

@Composable
fun NimbusProvider(json: String, nimbusService: NimbusService, ref: Ref<ServerDrivenView?>?) {
    nimbusService.start()
    val initialTree = nimbusService.createNodeFromJson(json = json)
    //TODO provide nimbusService to the UI tree using Composition Local
    //https://developer.android.com/jetpack/compose/compositionlocal
    val view = remember {  nimbusService.createView(serverDrivenNavigator) }

    val (currentTree, setCurrentTree) = remember { mutableStateOf(view.getCurrentTree()) }

    LaunchedEffect(true) {
        view.onChange {
            setCurrentTree(it)
        }
        if (ref != null) ref.current = view
        view.renderer.paint(initialTree)
    }

    currentTree?.let {
        NimbusView(viewTree = it, nimbusService)
    }
}

@Composable
fun NimbusView(viewTree: ServerDrivenNode, nimbusService: NimbusService) {
    if (!nimbusService.components.containsKey(viewTree.component)) {
        throw Error("Component with type ${viewTree.component} is not registered")
    }
    key(viewTree.id) {
        nimbusService.components[viewTree.component]!!(element = viewTree, children = {
            viewTree.children?.forEach {
                NimbusView(it, nimbusService = nimbusService)
            }
        })
    }
}
