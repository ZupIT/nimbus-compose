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

package br.com.zup.nimbus.compose.model

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import br.com.zup.nimbus.compose.Nimbus.Companion.instance
import br.com.zup.nimbus.compose.internal.HandleNimbusPageState
import br.com.zup.nimbus.core.ServerDrivenView
import br.com.zup.nimbus.core.tree.ServerDrivenNode
import br.com.zup.nimbus.core.tree.dynamic.node.RootNode
import kotlinx.coroutines.flow.MutableStateFlow

sealed class NimbusPageState {
    object PageStateOnLoading : NimbusPageState()
    data class PageStateOnError(val throwable: Throwable, val retry: () -> Unit) : NimbusPageState()
    data class PageStateOnShowPage(val node: ServerDrivenNode) : NimbusPageState()
}

data class Page(
    val id: String,
    val view: ServerDrivenView,
) {
    private var state: MutableStateFlow<NimbusPageState> =
        MutableStateFlow(NimbusPageState.PageStateOnLoading)
    private var testListener: ((NimbusPageState) -> Unit)? = null

//    // fixme: this is currently used only for testing. Instead, we should rewrite the test in order
//    //  to not need this.
    internal fun testOnChange(testListener: (NimbusPageState) -> Unit) {
        this.testListener = testListener
    }

    private fun setStateFlow(state: NimbusPageState) {
        this.state.value = state
    }

    private fun change(state: NimbusPageState) {
        setStateFlow(state)
        testListener?.let { it(state) }
    }

    fun setContent(tree: RootNode) {
        change(NimbusPageState.PageStateOnShowPage(tree))
    }

    fun setLoading() {
        change(NimbusPageState.PageStateOnLoading)
    }

    fun setError(throwable: Throwable, retry: () -> Unit) {
        change(NimbusPageState.PageStateOnError(throwable = throwable, retry = retry))
    }

    @Composable
    fun Compose() {
        val localState: NimbusPageState by state.collectAsState()
        localState.HandleNimbusPageState(instance.loadingView, instance.errorView)
    }
}

