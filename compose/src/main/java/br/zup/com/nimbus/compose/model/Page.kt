package br.zup.com.nimbus.compose.model

import br.zup.com.nimbus.compose.CoroutineDispatcherLib
import com.zup.nimbus.core.render.ServerDrivenView
import com.zup.nimbus.core.tree.ServerDrivenNode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

internal sealed class NimbusPageState {
    object PageStateOnLoading : NimbusPageState()
    data class PageStateOnError(val throwable: Throwable, val retry: () -> Unit) : NimbusPageState()
    data class PageStateOnShowPage(val serverDrivenNode: ServerDrivenNode) : NimbusPageState()
}

internal data class Page(
    val coroutineScope: CoroutineScope = CoroutineScope(CoroutineDispatcherLib.backgroundPool),
    val id: String, val view: ServerDrivenView,
) {
    // FIXME: Why do we need this?
    // 1. We don't need multiple listeners to update according to the state. Only a single
    // NimbusView must updated.
    // 2. We don't need to keep track of every state the view has reached, we only need to know
    // the current state. There should never be an overflow. We don't need a Buffer. This looks like
    // waste of memory to me.
    // 3. Why CoroutineDispatcherLib.REPLAY_COUNT is 5? Why would a listener need to know the 5
    // previous view states if it subscribed late? Only the most recent view state is important.
    // Shouldn't it be 1 instead?
    /*private var _content: MutableSharedFlow<NimbusPageState> =
        MutableSharedFlow(replay = CoroutineDispatcherLib.REPLAY_COUNT,
            onBufferOverflow = CoroutineDispatcherLib.ON_BUFFER_OVERFLOW)

    val content: SharedFlow<NimbusPageState>
        get() = _content

    init {
        view.onChange {
            setState(NimbusPageState.PageStateOnShowPage(it))
        }
    }*/

    private var setState: ((NimbusPageState) -> Unit)? = null

    fun onChange(listener: (NimbusPageState) -> Unit) {
        this.setState = listener
        view.onChange { setState?.let { set -> set(NimbusPageState.PageStateOnShowPage(it)) } }
    }

    /*private fun setState(nimbusPageState: NimbusPageState) {
        coroutineScope.launch(CoroutineDispatcherLib.backgroundPool) {
            _content.emit(nimbusPageState)
        }
    }*/

    fun setLoading() {
        setState?.let { it(NimbusPageState.PageStateOnLoading) }
    }

    fun setError(throwable: Throwable, retry: () -> Unit) {
        setState?.let { it(NimbusPageState.PageStateOnError(throwable = throwable, retry = retry)) }
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
