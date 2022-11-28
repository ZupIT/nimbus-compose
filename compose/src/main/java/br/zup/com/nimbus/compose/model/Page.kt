package br.zup.com.nimbus.compose.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import br.zup.com.nimbus.compose.Nimbus.Companion.instance
import br.zup.com.nimbus.compose.internal.HandleNimbusPageState
import com.zup.nimbus.core.ServerDrivenView
import com.zup.nimbus.core.tree.ServerDrivenNode
import com.zup.nimbus.core.tree.dynamic.node.RootNode
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

