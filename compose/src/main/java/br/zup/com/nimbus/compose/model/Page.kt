package br.zup.com.nimbus.compose.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.zup.nimbus.core.render.ServerDrivenView
import com.zup.nimbus.core.tree.ServerDrivenNode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal sealed class NimbusPageState {
    object PageStateOnLoading : NimbusPageState()
    class PageStateOnError(val throwable: Throwable, val retry: () -> Unit) : NimbusPageState()
    class PageStateOnShowPage(val serverDrivenNode: ServerDrivenNode) : NimbusPageState()
}

internal data class Page(val coroutineScope: CoroutineScope, val id: String, val view: ServerDrivenView) {
    var content: NimbusPageState by mutableStateOf(NimbusPageState.PageStateOnLoading)
        private set

    init {
        view.onChange {
            setState(NimbusPageState.PageStateOnShowPage(it))
        }
    }

    private fun setState(nimbusPageState: NimbusPageState) =
        coroutineScope.launch(Dispatchers.Default) {
            content = nimbusPageState
    }

    fun setLoading() {
        if (content !is NimbusPageState.PageStateOnLoading)
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
