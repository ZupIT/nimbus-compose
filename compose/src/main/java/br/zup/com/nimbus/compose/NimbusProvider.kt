package br.zup.com.nimbus.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import br.zup.com.nimbus.compose.ViewConstants.SHOW_VIEW_DESTINATION
import br.zup.com.nimbus.compose.ViewConstants.SHOW_VIEW_WITH_ARG
import br.zup.com.nimbus.compose.ViewConstants.VIEW_ID
import br.zup.com.nimbus.compose.serverdriven.Nimbus
import br.zup.com.nimbus.compose.serverdriven.NimbusTheme.nimbusAppState
import br.zup.com.nimbus.compose.serverdriven.ProvideNimbusAppState
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.zup.nimbus.core.ServerDrivenNavigator
import com.zup.nimbus.core.network.ViewRequest
import com.zup.nimbus.core.render.ServerDrivenView
import com.zup.nimbus.core.tree.RenderNode
import com.zup.nimbus.core.tree.ServerDrivenNode
import kotlinx.coroutines.launch
val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

class NimbusComposeNavigator(
    val initialUrl: String? = null,
    val navController: NavHostController
) : ServerDrivenNavigator {

    override fun dismiss() {
        TODO("Not yet implemented")
    }

    override fun pop() {
        navController.navigateUp()
    }

    override fun popTo(url: String) {
        TODO("Not yet implemented")
    }

    override fun present(request: ViewRequest) {
        TODO("Not yet implemented")
    }

    override fun push(request: ViewRequest) {
        val requestJson = convertViewRequestToJson(request)

        navController.navigate("${SHOW_VIEW_WITH_ARG}${requestJson}")
    }

}
//FIXME this should be passed as a parameter to the navigation
//var viewRequest: ViewRequest? = null
object ViewConstants {

    const val SHOW_VIEW = "show-view"
    const val VIEW_ID = "view-id"

    const val SHOW_VIEW_DESTINATION = "${SHOW_VIEW}?${VIEW_ID}={${VIEW_ID}}"
    const val SHOW_VIEW_WITH_ARG = "${SHOW_VIEW}?${VIEW_ID}="
}

@Composable
fun NimbusProvider(
    initialUrl: String,
    nimbus: Nimbus,
    ref: Ref<ServerDrivenView?>? = null
) {

    ProvideNimbusAppState(nimbusCompose = nimbus, initialUrl = initialUrl) {
        Scaffold { innerPaddingModifier ->

            NavHost(
                navController = nimbusAppState.navController,
                startDestination = SHOW_VIEW_DESTINATION,
                modifier = Modifier.padding(innerPaddingModifier)
            ) {
                composable(
                    route = SHOW_VIEW_DESTINATION,
                    arguments = listOf(navArgument(VIEW_ID) {
                        type = NavType.StringType
                        defaultValue = convertViewRequestToJson((ViewRequest(initialUrl)))
                    })
                ) { backStackEntry ->

                    val arguments = requireNotNull(backStackEntry.arguments)
                    val viewId = requireNotNull(arguments.getString(VIEW_ID))
                    val viewRequest = converViewJsonToObject(viewId)
                    val core = nimbusAppState.nimbusCompose.core
                    val view = nimbusAppState.nimbusCompose.core.createView(nimbusAppState.serverDrivenNavigator)
                    val (currentTree, setCurrentTree) = remember { mutableStateOf<ServerDrivenNode?>(view.renderer.getCurrentTree()) }
                    var initialTree: RenderNode? = null
                    nimbusAppState.coroutineScope.launch {

                        initialTree = core.viewClient.fetch(viewRequest!!)
                        view.renderer.paint(initialTree!!)
                    }

                    LaunchedEffect(true) {
                        view.onChange {
                            setCurrentTree(it)
                        }
                        if (ref != null) ref.current = view
                        initialTree?.let { view.renderer.paint(it) }
                    }

                    currentTree?.let {
                        RenderTree(viewTree = it)
                    }
                }

            }
        }
    }
}

@Composable
fun NimbusView(
    renderNode: RenderNode,
    view: ServerDrivenView,
    ref: Ref<ServerDrivenView?>? = null
) {

    val (currentTree, setCurrentTree) = remember { mutableStateOf<ServerDrivenNode?>(null) }

    LaunchedEffect(true) {
        view.onChange {
            setCurrentTree(it)
        }
        if (ref != null) ref.current = view
        view.renderer.paint(renderNode)
    }

    currentTree?.let {
        RenderTree(viewTree = it)
    }
}

@Composable
fun RenderTree(viewTree: ServerDrivenNode) {
    if (!nimbusAppState.nimbusCompose.components.containsKey(viewTree.component)) {
        throw Error("Component with type ${viewTree.component} is not registered")
    }
    key(viewTree.id) {
        nimbusAppState.nimbusCompose.components[viewTree.component]!!(
            element = viewTree,
            children = {
                viewTree.children?.forEach {
                    RenderTree(it)
                }
            })
    }
}

fun convertViewRequestToJson(request: ViewRequest): String? {
    val jsonAdapter = moshi.adapter(ViewRequest::class.java).lenient()
    val requestJson = jsonAdapter.toJson(request)
    return requestJson
}

fun converViewJsonToObject(json: String) : ViewRequest? {
    val jsonAdapter = moshi.adapter(ViewRequest::class.java).lenient()
    return jsonAdapter.fromJson(json)
}
