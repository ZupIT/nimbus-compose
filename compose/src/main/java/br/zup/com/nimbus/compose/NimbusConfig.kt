package br.zup.com.nimbus.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import br.zup.com.nimbus.compose.core.ui.components.ErrorDefault
import br.zup.com.nimbus.compose.core.ui.components.LoadingDefault
import br.zup.com.nimbus.compose.core.ui.internal.NimbusNavHostHelper
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

typealias ComponentHandler = (
    element: ServerDrivenNode,
    children: @Composable () -> Unit,
    parentElement: ServerDrivenNode?,
) -> Unit

typealias LoadingHandler = @Composable() () -> Unit
typealias ErrorHandler = @Composable() (throwable: Throwable, retry:() -> Unit) -> Unit
const val PLATFORM_NAME = "android"

@Stable
class NimbusComposeAppState(
    val config: NimbusConfig
)

@Stable
class NimbusNavigatorState(
    val navHostHelper: NimbusNavHostHelper
)

@Stable
class NimbusConfig(
    val baseUrl: String,
    val components: Map<String, @Composable ComponentHandler>,
    val actions: Map<String, ActionHandler>? = null,
    val operations: Map<String, OperationHandler>? = null,
    val logger: Logger? = null,
    val urlBuilder: UrlBuilder? = null,
    val httpClient: HttpClient? = null,
    val viewClient: ViewClient? = null,
    val idManager: IdManager? = null,
    val loadingView: LoadingHandler = { LoadingDefault() },
    val errorView: ErrorHandler = { throwable: Throwable, retry: () -> Unit ->
        ErrorDefault(throwable = throwable, retry = retry)
    },
) {

    val core = createNimbus()

    private val enviromentMap = mutableMapOf<String, Any>()

    private fun createNimbus() = Nimbus(config = createServerDrivenConfig())

    @Suppress("UNCHECKED_CAST")
    fun <T> enviromentObject(key: String): T? = enviromentMap[key] as T?

    fun <T> enviromentObject(key: String, value: T): NimbusConfig {
        enviromentMap[key] = value as Any
        return this
    }

    fun addOperations(operations: Map<String, OperationHandler>) {
        core.addOperations(operations)
    }

    fun addComponents(components: Map<String, @Composable ComponentHandler>) {
        this.components.plus(components)
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

private val LocalNavigator = staticCompositionLocalOf<NimbusNavigatorState> {
    error("No NimbusNavigator provided")
}

@Composable
fun Nimbus(
    config: NimbusConfig,
    content: @Composable () -> Unit
) {

    val nimbusComposeState = remember(
        config
    ) {
        NimbusComposeAppState(
            config = config
        )
    }
    CompositionLocalProvider(LocalNimbus provides nimbusComposeState, content = content)
}

@Composable
fun ProvideNavigatorState(
    navHostHelper: NimbusNavHostHelper,
    content: @Composable () -> Unit
) {

    val nimbusNavigatorState = remember(
        navHostHelper
    ) {
        NimbusNavigatorState(
            navHostHelper = navHostHelper
        )
    }
    CompositionLocalProvider(LocalNavigator provides nimbusNavigatorState, content = content)
}

object NimbusTheme {
    val nimbusAppState: NimbusComposeAppState
        @Composable
        get() = LocalNimbus.current

    val nimbusNavigatorState: NimbusNavigatorState
        @Composable
        get() = LocalNavigator.current
}
