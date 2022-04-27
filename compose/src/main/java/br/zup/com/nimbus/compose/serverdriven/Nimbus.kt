package br.zup.com.nimbus.compose.serverdriven

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.zup.nimbus.core.ActionHandler
import com.zup.nimbus.core.Nimbus
import com.zup.nimbus.core.OperationHandler
import com.zup.nimbus.core.ServerDrivenConfig
import com.zup.nimbus.core.tree.ServerDrivenNode
import br.zup.com.nimbus.compose.serverdriven.Nimbus as NimbusCompose

typealias ComponentHandler = (element: ServerDrivenNode, children: @Composable () -> Unit) -> Unit

@Stable
class Nimbus(
    baseUrl: String,
    components: Map<String, @Composable ComponentHandler>,
    actions: Map<String, ActionHandler>? = null,
    operations: Map<String, OperationHandler>? = null
) {

    var baseUrl by mutableStateOf(baseUrl)
        private set
    var components by mutableStateOf(components)
        private set
    var actions by mutableStateOf(actions)
        private set
    var operations by mutableStateOf(operations)
        private set

    var core by mutableStateOf(Nimbus(config = createServerDrivenConfig()))
        private set

    fun addOperations(operations: Map<String, OperationHandler>) {
        this.operations = this.operations?.plus(operations) ?: operations
    }

    fun addComponents(components: Map<String, @Composable ComponentHandler>) {
        this.components = this.components.plus(components)
    }

    fun addActions(actions: Map<String, ActionHandler>) {
        this.actions = this.actions?.plus(actions) ?: actions
    }

    private fun createServerDrivenConfig(): ServerDrivenConfig {
        return ServerDrivenConfig(
            platform = "android",
            baseUrl = baseUrl,
            operations = operations,
            actions = actions,
        )
    }
}

private val LocalNimbus = staticCompositionLocalOf<NimbusCompose> {
    error("No Nimbus provided")
}

@Composable
fun ProvideNimbus(
    nimbusCompose: NimbusCompose,
    content: @Composable () -> Unit
) {

    val nimbusComposeState = remember {
        nimbusCompose
    }
    CompositionLocalProvider(LocalNimbus provides nimbusComposeState, content = content)
}


object NimbusTheme {
    val nimbus: NimbusCompose
        @Composable
        get() = LocalNimbus.current
}
