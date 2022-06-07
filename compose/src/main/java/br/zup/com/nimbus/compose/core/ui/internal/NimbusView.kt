package br.zup.com.nimbus.compose.core.ui.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import br.zup.com.nimbus.compose.NimbusTheme
import br.zup.com.nimbus.compose.model.NimbusPageState
import br.zup.com.nimbus.compose.model.Page

@Composable
internal fun NimbusView(
    page: Page
) {
    var loading by remember { mutableStateOf(false) }
    page.content.let { nimbusPageState ->
        if (loading) {
            NimbusTheme.nimbusAppState.config.loadingView()
        }
        when (nimbusPageState) {
            is NimbusPageState.PageStateOnLoading -> {
                loading = true
            }
            is NimbusPageState.PageStateOnError -> {
                loading = false
                NimbusTheme.nimbusAppState.config.errorView(
                    nimbusPageState.throwable,
                    nimbusPageState.retry
                )
            }
            is NimbusPageState.PageStateOnShowPage -> {
                NimbusServerDrivenView(viewTree = nimbusPageState.serverDrivenNode)
                loading = false
            }
        }
    }
}
