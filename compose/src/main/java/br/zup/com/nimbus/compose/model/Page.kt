package br.zup.com.nimbus.compose.model

import com.zup.nimbus.core.render.ServerDrivenView
import com.zup.nimbus.core.tree.ServerDrivenNode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal sealed class NimbusPageState {
    object PageStateOnLoading : NimbusPageState()
    class PageStateOnError(val throwable: Throwable, val retry: () -> Unit) : NimbusPageState()
    class PageStateOnShowPage(val serverDrivenNode: ServerDrivenNode) : NimbusPageState()
}

internal data class Page(
    val id: String, val view: ServerDrivenView,
) {

    private var _content: MutableStateFlow<NimbusPageState> =
        MutableStateFlow(NimbusPageState.PageStateOnLoading)

    val content: StateFlow<NimbusPageState>
        get() = _content

    init {
        view.onChange {
            setState(NimbusPageState.PageStateOnShowPage(it))
        }
    }

    private fun setState(nimbusPageState: NimbusPageState) {
        _content.value = nimbusPageState
    }

    fun setLoading() {
        setState(NimbusPageState.PageStateOnLoading)
    }

    fun setError(throwable: Throwable, retry: () -> Unit) {
        setState(NimbusPageState.PageStateOnError(throwable = throwable, retry = retry))
    }
}

internal fun Page.removePagesAfter(pages: MutableList<Page>) {
    val index = pages.indexOf(this)
    if (index < pages.lastIndex)
        pages.subList(index + 1, pages.size).dispose().clear()
}


internal fun MutableList<Page>.removeLastPage(): Page = removeLast().also { page -> page.dispose() }
internal fun MutableList<Page>.removeAllPages(): Unit = this.dispose().clear()

internal fun MutableList<Page>.dispose(): MutableList<Page> {
    return this.also {
        forEach {
            it.dispose()
        }
    }
}

internal fun Page.dispose(): Page = this.also {
    view.destroy()
}
