package br.zup.com.nimbus.compose.serverdriven

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.zup.nimbus.core.Nimbus
import com.zup.nimbus.core.ServerDrivenConfig
import com.zup.nimbus.core.tree.ServerDrivenNode
import br.zup.com.nimbus.compose.serverdriven.Nimbus as NimbusCompose

@Stable
class Nimbus(
    //TODO fill in the other properties
    val baseUrl: String,
    val components: Map<String, @Composable (element: ServerDrivenNode, children: @Composable () -> Unit) -> Unit>,
) {

    lateinit var core: Nimbus

    fun start() {
        val serverDrivenConfig = createServerDrivenConfig()

        core = Nimbus(config = serverDrivenConfig)
    }

    private fun createServerDrivenConfig(): ServerDrivenConfig {

        return ServerDrivenConfig(baseUrl, platform = "android")
    }
}

private val LocalNimbus = staticCompositionLocalOf<NimbusCompose> {
    error("No NimbusService provided")
}

@Composable
fun ProvideNimbus(
    nimbus: NimbusCompose,
    content: @Composable () -> Unit
) {
    nimbus.start()
    val nimbusComposeState = remember {
        nimbus
    }
    CompositionLocalProvider(LocalNimbus provides nimbusComposeState, content = content)
}


object NimbusTheme {
    val nimbus: br.zup.com.nimbus.compose.serverdriven.Nimbus
        @Composable
        get() = LocalNimbus.current
}
