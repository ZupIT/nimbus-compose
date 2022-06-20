package br.zup.com.nimbus.compose.core.ui.internal

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import br.zup.com.nimbus.compose.CoroutineDispatcherLib
import br.zup.com.nimbus.compose.NimbusTheme
import br.zup.com.nimbus.compose.model.NimbusPageState
import br.zup.com.nimbus.compose.model.Page
import kotlinx.coroutines.withContext

@Composable
internal fun NimbusView(
    page: Page,
) {
    var loading by remember { mutableStateOf(true) }

    RenderPageState(page) { isLoading ->
        if (loading != isLoading)
            loading = isLoading
    }

    if (loading) {
        NimbusTheme.nimbusAppState.config.loadingView()
    }
}

@Composable
internal fun RenderPageState(
    page: Page,
    onLoading: (Boolean) -> Unit,
) {
    val nimbusPageState: MutableState<NimbusPageState> = remember {
        mutableStateOf(NimbusPageState.PageStateOnLoading)
    }

    with(nimbusPageState) {
        val state: NimbusPageState = this.value
        when (state) {
            is NimbusPageState.PageStateOnLoading -> {
                onLoading(true)
            }
            is NimbusPageState.PageStateOnError -> {
                onLoading(false)
                NimbusTheme.nimbusAppState.config.errorView(
                    state.throwable,
                    state.retry
                )
            }
            is NimbusPageState.PageStateOnShowPage -> {
                NimbusServerDrivenView(viewTree = state.serverDrivenNode)
                Spacer(modifier = Modifier.semantics(true) {
                    testTag = "NimbusPage:${page.id}"
                })
                onLoading(false)
            }
        }
    }

    LaunchedEffect(key1 = Unit) {
        withContext(CoroutineDispatcherLib.backgroundPool) {
            page.content.collect { pageState ->
                withContext(CoroutineDispatcherLib.mainThread) {
                    nimbusPageState.value = pageState
                }
            }
        }
    }
}
