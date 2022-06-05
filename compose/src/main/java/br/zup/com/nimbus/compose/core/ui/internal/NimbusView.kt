package br.zup.com.nimbus.compose.core.ui.internal

import androidx.compose.runtime.Composable
import br.zup.com.nimbus.compose.NimbusTheme
import br.zup.com.nimbus.compose.ScreenLoadingHandler
import br.zup.com.nimbus.compose.model.NimbusPageState
import br.zup.com.nimbus.compose.model.Page

@Composable
internal fun NimbusView(
    page: Page,
    screenLoadingHandler: ScreenLoadingHandler = {}
) {
    page.content.let { nimbusPageState ->
        when (nimbusPageState) {
            is NimbusPageState.PageStateOnLoading -> {
                screenLoadingHandler(true)
                NimbusTheme.nimbusAppState.config.loadingView()
            }
            is NimbusPageState.PageStateOnError -> {
                NimbusTheme.nimbusAppState.config.errorView(
                    nimbusPageState.throwable,
                    nimbusPageState.retry
                )
            }
            is NimbusPageState.PageStateOnShowPage -> {
                NimbusServerDrivenView(viewTree = nimbusPageState.serverDrivenNode)
                screenLoadingHandler(false)
            }
        }
    }
}
