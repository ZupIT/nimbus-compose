/*
 * Copyright 2023 ZUP IT SERVICOS EM TECNOLOGIA E INOVACAO SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.zup.nimbus.compose

import br.com.zup.nimbus.core.Nimbus
import br.com.zup.nimbus.core.ServerDrivenConfig
import br.com.zup.nimbus.core.log.LogLevel
import br.com.zup.nimbus.core.log.Logger
import br.com.zup.nimbus.core.network.HttpClient
import br.com.zup.nimbus.core.network.ServerDrivenRequest
import br.com.zup.nimbus.core.network.ServerDrivenResponse

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

object Nimbus {
    val instance = NimbusCompose
}
