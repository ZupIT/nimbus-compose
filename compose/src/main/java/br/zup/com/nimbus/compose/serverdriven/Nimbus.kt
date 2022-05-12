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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import br.zup.com.nimbus.compose.NimbusComposeNavigator
import br.zup.com.nimbus.compose.NimbusViewModel
import br.zup.com.nimbus.compose.components.ErrorDefault
import br.zup.com.nimbus.compose.components.LoadingDefault
import com.zup.nimbus.core.ActionHandler
import com.zup.nimbus.core.Nimbus
import com.zup.nimbus.core.OperationHandler
import com.zup.nimbus.core.ServerDrivenConfig
import com.zup.nimbus.core.log.Logger
import com.zup.nimbus.core.network.HttpClient
import com.zup.nimbus.core.network.UrlBuilder
import com.zup.nimbus.core.network.ViewClient
import com.zup.nimbus.core.tree.IdManager
import com.zup.nimbus.core.tree.ServerDrivenNode
import kotlinx.coroutines.CoroutineScope
import br.zup.com.nimbus.compose.serverdriven.Nimbus as NimbusCompose

typealias ComponentHandler = (element: ServerDrivenNode, children: @Composable () -> Unit) -> Unit
typealias LoadingHandler = @Composable() () -> Unit
typealias ErrorHandler = @Composable() (throwable: Throwable) -> Unit
const val PLATFORM_NAME = "android"

@Stable
class NimbusComposeAppState(
    val nimbusCompose: NimbusCompose,
    val navController: NavHostController,
    val nimbusViewModel: NimbusViewModel,
    val coroutineScope: CoroutineScope
) {

    init {
        nimbusViewModel.initFirstView()
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
    loadingView: LoadingHandler = { LoadingDefault()},
    errorView: ErrorHandler = { ErrorDefault(throwable = it)}
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

    var loadingView by mutableStateOf(loadingView)
        private set

    var errorView by mutableStateOf(errorView)
        private set

    var core by mutableStateOf(
        createNimbus()
    )

    private fun createNimbus() = Nimbus(config = createServerDrivenConfig())

    fun addOperations(operations: Map<String, OperationHandler>) {
        core.addOperations(operations)
    }

    fun addComponents(components: Map<String, @Composable ComponentHandler>) {
        this.components = this.components.plus(components)
    }

    fun addActions(actions: Map<String, ActionHandler>) {
        core.addActions(actions)
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
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    content: @Composable () -> Unit
) {

    val nimbusViewModel: NimbusViewModel = viewModel(
        factory = NimbusViewModel.provideFactory(
            navController = navController,
            initialUrl = initialUrl,
            nimbusComposeNavigator = NimbusComposeNavigator(
                nimbusCompose = nimbusCompose,
                coroutineScope = coroutineScope
            )
        )
    )

    val nimbusComposeState = remember(
        initialUrl,
        nimbusCompose,
        scaffoldState,
        navController,
        coroutineScope,
        nimbusViewModel,
    ) {
        NimbusComposeAppState(
            nimbusCompose = nimbusCompose,
            navController = navController,
            coroutineScope = coroutineScope,
            nimbusViewModel = nimbusViewModel
        )
    }
    CompositionLocalProvider(LocalNimbus provides nimbusComposeState, content = content)
}


object NimbusTheme {
    val nimbusAppState: NimbusComposeAppState
        @Composable
        get() = LocalNimbus.current
}
