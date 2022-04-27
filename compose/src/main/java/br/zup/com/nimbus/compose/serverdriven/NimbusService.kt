package br.zup.com.nimbus.compose.serverdriven

import androidx.compose.runtime.Composable
import com.zup.nimbus.core.Nimbus
import com.zup.nimbus.core.ServerDrivenConfig
import com.zup.nimbus.core.action.ServerDrivenNavigator
import com.zup.nimbus.core.tree.ServerDrivenNode

class NimbusService(
    //TODO fill in the other properties
    val baseUrl: String,
    val components: Map<String, @Composable (element: ServerDrivenNode, children: @Composable () -> Unit) -> Unit>,
) {

    private lateinit var nimbus: Nimbus

    fun start() {
        val serverDrivenConfig = createServerDrivenConfig()

        nimbus = Nimbus(config = serverDrivenConfig)
    }

    private fun createServerDrivenConfig(): ServerDrivenConfig {

        return ServerDrivenConfig(baseUrl, platform = "android")
    }


    fun createNodeFromJson(json: String) = nimbus.createNodeFromJson(json)

    fun createView(serverDrivenNavigator: ServerDrivenNavigator) =
        nimbus.createView(serverDrivenNavigator)
}