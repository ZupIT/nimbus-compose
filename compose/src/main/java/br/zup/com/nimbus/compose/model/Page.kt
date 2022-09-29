package br.zup.com.nimbus.compose.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import br.zup.com.nimbus.compose.CoroutineDispatcherLib
import br.zup.com.nimbus.compose.NimbusTheme
import br.zup.com.nimbus.compose.internal.NodeFlow
import br.zup.com.nimbus.compose.internal.RenderedNode
import com.zup.nimbus.core.ServerDrivenView
import com.zup.nimbus.core.tree.ServerDrivenNode
import com.zup.nimbus.core.tree.dynamic.node.RootNode
import kotlinx.coroutines.CoroutineScope

sealed class NimbusPageState {
    object PageStateOnLoading : NimbusPageState()
    data class PageStateOnError(val throwable: Throwable, val retry: () -> Unit) : NimbusPageState()
    data class PageStateOnShowPage(val node: ServerDrivenNode) : NimbusPageState()
}

data class Page(
    val coroutineScope: CoroutineScope = CoroutineScope(CoroutineDispatcherLib.backgroundPool),
    val id: String,
    val view: ServerDrivenView,
) {
    private var state: NimbusPageState = NimbusPageState.PageStateOnLoading
    private var listener: (() -> Unit)? = null
    private var testListener: ((NimbusPageState) -> Unit)? = null

    // fixme: this is currently used only for testing. Instead, we should rewrite the test in order
    //  to not need this.
    internal fun testOnChange(testListener: (NimbusPageState) -> Unit) {
        this.testListener = testListener
    }

    private fun change(state: NimbusPageState) {
        this.state = state
        listener?.let { it() }
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
        val (localState, setState) = remember { mutableStateOf(state) }
        listener = { setState(state) }

        when (localState) {
            is NimbusPageState.PageStateOnLoading -> {
                NimbusTheme.nimbus.loadingView()
            }
            is NimbusPageState.PageStateOnError -> {
                NimbusTheme.nimbus.errorView(localState.throwable, localState.retry)
            }
            is NimbusPageState.PageStateOnShowPage -> {
                RenderedNode(flow = NodeFlow(localState.node))
            }
        }
    }
}

internal fun Page.removePagesAfter(pages: MutableList<Page>) {
    val index = pages.indexOf(this)
    if (index < pages.lastIndex)
        pages.subList(index + 1, pages.size).clear()
}
