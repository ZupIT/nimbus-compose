package br.zup.com.nimbus.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import br.zup.com.nimbus.compose.core.ui.components.ErrorDefault
import br.zup.com.nimbus.compose.core.ui.components.LoadingDefault
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

typealias ComponentHandler = (element: ServerDrivenNode, children: @Composable () -> Unit) -> Unit
typealias LoadingHandler = @Composable() () -> Unit
typealias ErrorHandler = @Composable() (throwable: Throwable) -> Unit
const val PLATFORM_NAME = "android"

@Stable
internal class NimbusComposeAppState(
    val nimbusConfig: NimbusConfig,
    val coroutineScope: CoroutineScope
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
    val loadingView: LoadingHandler = { LoadingDefault()},
    val errorView: ErrorHandler = { ErrorDefault(throwable = it)}
) {

    var core by mutableStateOf(
        createNimbus()
    )

    private fun createNimbus() = Nimbus(config = createServerDrivenConfig())

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

@Composable
fun Nimbus(
    nimbusConfig: NimbusConfig,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    content: @Composable () -> Unit
) {

    val nimbusComposeState = remember(
        nimbusConfig,
        coroutineScope,
    ) {
        NimbusComposeAppState(
            nimbusConfig = nimbusConfig,
            coroutineScope = coroutineScope
        )
    }
    CompositionLocalProvider(LocalNimbus provides nimbusComposeState, content = content)
}


internal object NimbusTheme {
    val nimbusAppState: NimbusComposeAppState
        @Composable
        get() = LocalNimbus.current
}
