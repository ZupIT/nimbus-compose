package br.zup.com.nimbus.compose.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.zup.nimbus.core.render.ServerDrivenView
import com.zup.nimbus.core.tree.ServerDrivenNode

internal sealed class NimbusPageState {
    object PageStateOnLoading : NimbusPageState()
    class PageStateOnError(val throwable: Throwable) : NimbusPageState()
    class PageStateOnShowPage(val serverDrivenNode: ServerDrivenNode) : NimbusPageState()
}

internal data class Page(val id: String, val view: ServerDrivenView) {
    var content: NimbusPageState? by mutableStateOf(null)
    init {
        content = NimbusPageState.PageStateOnLoading
        view.onChange {
            content = NimbusPageState.PageStateOnShowPage(it)
        }
    }
}