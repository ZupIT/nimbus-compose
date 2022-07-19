package br.zup.com.nimbus.compose

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import br.zup.com.nimbus.compose.core.ui.components.ErrorDefault
import br.zup.com.nimbus.compose.core.ui.components.LoadingDefault
import br.zup.com.nimbus.compose.core.ui.internal.NimbusNavHostHelper
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
import com.zup.nimbus.core.tree.ObservableState
import com.zup.nimbus.core.tree.ServerDrivenNode
import br.zup.com.nimbus.compose.Nimbus as NimbusCompose

typealias ComponentHandler = @Composable (ComponentData) -> Unit
typealias LoadingHandler = @Composable () -> Unit
typealias ErrorHandler = @Composable (throwable: Throwable, retry: () -> Unit) -> Unit

const val PLATFORM_NAME = "android"

@Stable
class NimbusNavigatorState(
    val navHostHelper: NimbusNavHostHelper,
)

enum class NimbusMode { Development, Release }

@Stable
class Nimbus(
    val baseUrl: String,
    components: List<ComponentLibrary> = emptyList(),
    val actions: Map<String, ActionHandler>? = null,
    val actionObservers: List<ActionHandler>? = null,
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
    val mode: NimbusMode? = NimbusMode.Development
) {

    internal val core = createNimbus()

    val components = HashMap<String, ComponentHandler>()

    val globalState: ObservableState by lazy { core.globalState }

    private val enviromentMap = mutableMapOf<String, Any>()

    private fun createNimbus() = Nimbus(config = createServerDrivenConfig())

    init {
        components.forEach { lib -> this.components.putAll(lib.components) }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> enviromentObject(key: String): T? = enviromentMap[key] as T?

    fun <T> enviromentObject(key: String, value: T): NimbusCompose {
        enviromentMap[key] = value as Any
        return this
    }

    fun createView(getNavigator: () -> ServerDrivenNavigator, description: String? = null) =
        core.createView(getNavigator = getNavigator, description = description)

    fun createNodeFromJson(json: String) = core.createNodeFromJson(json = json)

    fun addOperations(operations: Map<String, OperationHandler>) {
        core.addOperations(operations)
    }

    fun addComponentLibrary(library: ComponentLibrary) {
        this.components.putAll(library.components)
    }

    fun addActions(actions: Map<String, ActionHandler>) {
        core.addActions(actions)
    }

    fun addActionObservers(observers: List<ActionHandler>) = core.addActionObservers(
        observers = observers
    )

    private fun createServerDrivenConfig(): ServerDrivenConfig {
        return ServerDrivenConfig(
            platform = PLATFORM_NAME,
            baseUrl = baseUrl,
            actions = actions,
            actionObservers = actionObservers,
            operations = operations,
            logger = logger,
            urlBuilder = urlBuilder,
            httpClient = httpClient,
            viewClient = viewClient,
            idManager = idManager
        )
    }
}

private val LocalNimbus = staticCompositionLocalOf<NimbusCompose> {
    error("No Nimbus provided")
}

private val LocalNavigator = staticCompositionLocalOf<NimbusNavigatorState> {
    error("No NimbusNavigator provided")
}

@Composable
fun ProvideNimbus(
    nimbus: NimbusCompose,
    applicationContext: Context = LocalContext.current.applicationContext,
    content: @Composable () -> Unit,
) {
    configureStaticState(applicationContext)

    val nimbusComposeState = remember(
        nimbus
    ) {
        nimbus
    }
    CompositionLocalProvider(LocalNimbus provides nimbusComposeState, content = content)
}

private fun configureStaticState(applicationContext: Context) {
    if (NimbusTheme.nimbusStaticState == null) {
        NimbusTheme.nimbusStaticState =
            NimbusComposeStaticState(applicationContext = applicationContext)
    }
}

@Composable
fun ProvideNavigatorState(
    navHostHelper: NimbusNavHostHelper,
    content: @Composable () -> Unit,
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

/**
 * Exposes a singleton state for non compose functions.
 * Should only expose singleton properties here
 */
class NimbusComposeStaticState(val applicationContext: Context)

object NimbusTheme {

    @get:Synchronized
    @set:Synchronized
    var nimbusStaticState: NimbusComposeStaticState? = null
        internal set

    val nimbus: NimbusCompose
        @Composable
        get() = LocalNimbus.current

    val nimbusNavigatorState: NimbusNavigatorState
        @Composable
        get() = LocalNavigator.current
}
