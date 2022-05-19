package br.zup.com.nimbus.compose.core.ui.internal

import androidx.compose.runtime.Composable
import br.zup.com.nimbus.compose.NimbusTheme
import br.zup.com.nimbus.compose.core.ui.NimbusServerDrivenView
import br.zup.com.nimbus.compose.model.NimbusPageState
import br.zup.com.nimbus.compose.model.Page

@Composable
internal fun NimbusView(
    page: Page,
    onStart: () -> Unit = {},
    onCreate: () -> Unit = {},
    onDestroy: () -> Unit = {},
) {
    NimbusDisposableEffect(
        onStart = onStart,
        onCreate = onCreate,
        onDestroy = {
            page.view.destroy()
            onDestroy()
        })

    page.content.let { nimbusPageState ->
        when (nimbusPageState) {
            is NimbusPageState.PageStateOnLoading -> {
                NimbusTheme.nimbusAppState.config.loadingView()
            }
            is NimbusPageState.PageStateOnError -> {
                NimbusTheme.nimbusAppState.config.errorView(nimbusPageState.throwable)
            }
            is NimbusPageState.PageStateOnShowPage -> {
                NimbusServerDrivenView(viewTree = nimbusPageState.serverDrivenNode)
            }
        }
    }
}