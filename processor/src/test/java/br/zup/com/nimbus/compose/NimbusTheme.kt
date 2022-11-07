package br.zup.com.nimbus.compose

import com.zup.nimbus.core.Nimbus
import com.zup.nimbus.core.ServerDrivenConfig
import com.zup.nimbus.core.log.LogLevel
import com.zup.nimbus.core.log.Logger
import com.zup.nimbus.core.network.HttpClient
import com.zup.nimbus.core.network.ServerDrivenRequest
import com.zup.nimbus.core.network.ServerDrivenResponse

enum class NimbusMode { Development, Release }

object MockLogger: Logger {
    var errors = mutableListOf<String>()
    var warnings = mutableListOf<String>()
    var infos = mutableListOf<String>()
    override fun disable() {}
    override fun enable() {}
    override fun error(message: String) { errors.add(message) }
    override fun info(message: String) { infos.add(message) }
    override fun warn(message: String) { warnings.add(message) }

    override fun log(message: String, level: LogLevel) {
        when(level) {
            LogLevel.Error -> error(message)
            LogLevel.Info -> info(message)
            LogLevel.Warning -> warn(message)
        }
    }

    fun clear() {
        errors = mutableListOf()
        infos = mutableListOf()
        warnings = mutableListOf()
    }
}

private object MockHttpClient: HttpClient {
    override suspend fun sendRequest(request: ServerDrivenRequest): ServerDrivenResponse {
        throw NotImplementedError("No need to implement this for KSP testing.")
    }
}

object NimbusCompose: Nimbus(
    ServerDrivenConfig(
        "",
        "test",
        httpClient = MockHttpClient,
        logger = MockLogger,
    )
) {
    var mode = NimbusMode.Development
}

object NimbusTheme {
    val nimbus = NimbusCompose
}
