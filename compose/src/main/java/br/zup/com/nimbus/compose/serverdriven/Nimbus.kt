package br.zup.com.nimbus.compose.serverdriven

import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import br.zup.com.nimbus.compose.NimbusComposeNavigator
import com.zup.nimbus.core.ActionHandler
import com.zup.nimbus.core.Nimbus
import com.zup.nimbus.core.OperationHandler
import com.zup.nimbus.core.ServerDrivenConfig
import com.zup.nimbus.core.ServerDrivenNavigator
import com.zup.nimbus.core.log.Logger
import com.zup.nimbus.core.network.HttpClient
import com.zup.nimbus.core.network.UrlBuilder
import com.zup.nimbus.core.network.ViewClient
import com.zup.nimbus.core.tree.IdManager
import com.zup.nimbus.core.tree.ServerDrivenNode
import kotlinx.coroutines.CoroutineScope
import br.zup.com.nimbus.compose.serverdriven.Nimbus as NimbusCompose

typealias ComponentHandler = (element: ServerDrivenNode, children: @Composable () -> Unit) -> Unit

const val PLATFORM_NAME = "android"

@Stable
class NimbusComposeAppState(
    val scaffoldState: ScaffoldState,
    val nimbusCompose: NimbusCompose,
    val navController: NavHostController,
    val serverDrivenNavigator: ServerDrivenNavigator,
    val coroutineScope: CoroutineScope
) {
    val currentRoute: String?
        get() = navController.currentDestination?.route

    fun pop() {
        navController.navigateUp()
    }

    fun popTo(url: String) {
        if (removeFromStackMatchingArg(
                navController = navController,
                arg = "viewId",
                argValue = url,
                inclusive = true
            )
        ) {
            navController.navigate("showScreen?viewId=${url}")
        }
    }

    fun push(url: String) {
        navController.navigate("showScreen?viewId=${url}")
    }

    private fun removeFromStackMatchingArg(
        navController: NavHostController,
        arg: String,
        argValue: Any?,
        inclusive: Boolean = false
    ): Boolean {
        var elementFound = false
        val removeList = mutableListOf<NavBackStackEntry>()
        for (item in navController.backQueue.reversed()) {
            if (item.destination.route == navController.graph.startDestinationRoute) {
                if (item.arguments?.getString(
                        arg
                    ) == argValue
                ) {
                    if(inclusive) {
                        removeList.add(item)
                    }
                    elementFound = true
                    break
                } else {
                    removeList.add(item)
                }
            }
        }

        if(elementFound) {
            navController.backQueue.removeAll(removeList)
        }
        return elementFound
    }

}

@Stable
class Nimbus(
    baseUrl: String,
    components: Map<String, @Composable ComponentHandler>,
    actions: Map<String, ActionHandler>? = null,
    operations: Map<String, OperationHandler>? = null,
    logger: Logger? = null,
    urlBuilder: UrlBuilder? = null,
    httpClient: HttpClient? = null,
    viewClient: ViewClient? = null,
    idManager: IdManager? = null,
) {

    var baseUrl by mutableStateOf(baseUrl)
        private set
    var components by mutableStateOf(components)
        private set
    var actions by mutableStateOf(actions)
        private set
    var operations by mutableStateOf(operations)
        private set

    var logger by mutableStateOf(logger)
        private set

    var urlBuilder by mutableStateOf(urlBuilder)
        private set

    var httpClient by mutableStateOf(httpClient)
        private set

    var viewClient by mutableStateOf(viewClient)
        private set

    var idManager by mutableStateOf(idManager)
        private set

    var core by mutableStateOf(
        createNimbus()
    )

    private fun createNimbus() = Nimbus(config = createServerDrivenConfig())

    fun addOperations(operations: Map<String, OperationHandler>) {
        this.operations = this.operations?.plus(operations) ?: operations
        core = createNimbus()
    }

    fun addComponents(components: Map<String, @Composable ComponentHandler>) {
        this.components = this.components.plus(components)
        core = createNimbus()
    }

    fun addActions(actions: Map<String, ActionHandler>) {
        this.actions = this.actions?.plus(actions) ?: actions
        core = createNimbus()
    }

    private fun createServerDrivenConfig(): ServerDrivenConfig {
        return ServerDrivenConfig(
            platform = PLATFORM_NAME,
            baseUrl = baseUrl,
            actions = actions,
            operations = operations,
            logger = logger,
            urlBuilder = urlBuilder,
            httpClient = httpClient,
            viewClient = viewClient,
            idManager = idManager
        )
    }
}

private val LocalNimbus = staticCompositionLocalOf<NimbusComposeAppState> {
    error("No Nimbus provided")
}

@Composable
fun ProvideNimbusAppState(
    initialUrl: String,
    nimbusCompose: NimbusCompose,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    navController: NavHostController = rememberNavController(),
    serverDrivenNavigator: ServerDrivenNavigator = NimbusComposeNavigator(
        initialUrl = initialUrl,
        navController = navController
    ),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    content: @Composable () -> Unit
) {

    val nimbusComposeState = remember(
        nimbusCompose,
        navController,
        scaffoldState,
        coroutineScope,
        serverDrivenNavigator
    ) {
        NimbusComposeAppState(
            nimbusCompose = nimbusCompose,
            navController = navController,
            serverDrivenNavigator = serverDrivenNavigator,
            scaffoldState = scaffoldState,
            coroutineScope = coroutineScope
        )
    }
    CompositionLocalProvider(LocalNimbus provides nimbusComposeState, content = content)
}


object NimbusTheme {
    val nimbusAppState: NimbusComposeAppState
        @Composable
        get() = LocalNimbus.current
}
