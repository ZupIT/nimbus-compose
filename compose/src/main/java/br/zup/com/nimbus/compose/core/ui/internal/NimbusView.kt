package br.zup.com.nimbus.compose.core.ui.internal

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import br.zup.com.nimbus.compose.NimbusTheme
import br.zup.com.nimbus.compose.model.NimbusPageState
import br.zup.com.nimbus.compose.model.Page

@Composable
internal fun NimbusView(
    page: Page,
) {
    var loading by remember { mutableStateOf(true) }

    RenderPageState(page.content, page) {
        loading = false
    }

    if (loading) {
        NimbusTheme.nimbusAppState.config.loadingView()
    }
}

@Composable
private fun RenderPageState(
    nimbusPageState: NimbusPageState,
    page: Page,
    onDone: () -> Unit,
) {
    when (nimbusPageState) {
        is NimbusPageState.PageStateOnLoading -> {

        }
        is NimbusPageState.PageStateOnError -> {
            onDone()
            NimbusTheme.nimbusAppState.config.errorView(
                nimbusPageState.throwable,
                nimbusPageState.retry
            )
        }
        is NimbusPageState.PageStateOnShowPage -> {
            NimbusServerDrivenView(viewTree = nimbusPageState.serverDrivenNode)
            Spacer(modifier = Modifier.semantics(true) {
                testTag = "NimbusPage:${page.id}"
            })
            onDone()
        }
    }
}
