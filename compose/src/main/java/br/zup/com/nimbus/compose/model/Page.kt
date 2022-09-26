package br.zup.com.nimbus.compose.model

import br.zup.com.nimbus.compose.CoroutineDispatcherLib
import br.zup.com.nimbus.compose.core.ui.internal.ObservableNode
import com.zup.nimbus.core.ServerDrivenView
import com.zup.nimbus.core.tree.ServerDrivenNode
import com.zup.nimbus.core.tree.dynamic.node.RootNode
import kotlinx.coroutines.CoroutineScope

sealed class NimbusPageState {
    object PageStateOnLoading : NimbusPageState()
    data class PageStateOnError(val throwable: Throwable, val retry: () -> Unit) : NimbusPageState()
    data class PageStateOnShowPage(val observableNode: ObservableNode) : NimbusPageState()
}

data class Page(
    val coroutineScope: CoroutineScope = CoroutineScope(CoroutineDispatcherLib.backgroundPool),
    val id: String, val view: ServerDrivenView,
) {
    private var setState: ((NimbusPageState) -> Unit)? = null

    fun onChange(listener: (NimbusPageState) -> Unit) {
        this.setState = listener
    }

    fun setContent(tree: RootNode) {
        setState?.let { it(NimbusPageState.PageStateOnShowPage(ObservableNode(tree))) }
    }

    fun setLoading() {
        setState?.let { it(NimbusPageState.PageStateOnLoading) }
    }

    fun setError(throwable: Throwable, retry: () -> Unit) {
        setState?.let { it(NimbusPageState.PageStateOnError(throwable = throwable, retry = retry)) }
    }
}
