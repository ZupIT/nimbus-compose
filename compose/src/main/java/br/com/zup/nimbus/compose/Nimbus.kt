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

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import br.com.zup.nimbus.compose.Nimbus.Companion.staticState
import br.com.zup.nimbus.compose.internal.NimbusNavHostHelper
import br.com.zup.nimbus.compose.ui.NimbusComposeUILibrary
import br.com.zup.nimbus.compose.ui.components.ErrorDefault
import br.com.zup.nimbus.compose.ui.components.LoadingDefault
import br.com.zup.nimbus.compose.ui.composeUILibrary
import br.com.zup.nimbus.core.Nimbus
import br.com.zup.nimbus.core.ServerDrivenConfig
import br.com.zup.nimbus.core.log.Logger
import br.com.zup.nimbus.core.network.HttpClient
import br.com.zup.nimbus.core.network.UrlBuilder
import br.com.zup.nimbus.core.network.ViewClient
import br.com.zup.nimbus.core.tree.IdManager
import br.com.zup.nimbus.compose.Nimbus as NimbusCompose

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
@Suppress("LongParameterList")
class Nimbus(
    val baseUrl: String,
    logger: Logger? = null,
    urlBuilder: ((String) -> UrlBuilder)? = null,
    httpClient: HttpClient? = null,
    viewClient: ((nimbus: Nimbus) -> ViewClient)? = null,
    idManager: IdManager? = null,
    ui: List<NimbusComposeUILibrary>? = null,
    val loadingView: LoadingHandler = { LoadingDefault() },
    val errorView: ErrorHandler = { throwable: Throwable, retry: () -> Unit ->
        ErrorDefault(throwable = throwable, retry = retry)
    },
    val mode: NimbusMode? = NimbusMode.Development
): Nimbus(ServerDrivenConfig(
    platform = PLATFORM_NAME,
    baseUrl = baseUrl,
    logger = logger,
    ui = ui,
    coreUILibrary = composeUILibrary,
    urlBuilder = urlBuilder,
    httpClient = httpClient,
    viewClient = viewClient,
    idManager = idManager
)) {
    companion object {
        @get:Synchronized
        @set:Synchronized
        var staticState: NimbusComposeStaticState? = null
            internal set

        val instance: NimbusCompose
            @Composable
            get() = LocalNimbus.current

        val navigatorInstance: NimbusNavigatorState
            @Composable
            get() = LocalNavigator.current
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
    context: Context = LocalContext.current,
    content: @Composable () -> Unit,
) {
    configureStaticState(context)

    val nimbusComposeState = remember(
        nimbus
    ) {
        nimbus
    }
    CompositionLocalProvider(LocalNimbus provides nimbusComposeState, content = content)
}

private fun configureStaticState(context: Context) {
    if (staticState == null) {
        staticState =
            NimbusComposeStaticState(
            context = context)
    }
}

@Composable
internal fun ProvideNavigatorState(
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
class NimbusComposeStaticState(val context: Context)
