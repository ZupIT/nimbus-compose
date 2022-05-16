package br.zup.com.nimbus.compose.core.ui.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import br.zup.com.nimbus.compose.NimbusTheme
import br.zup.com.nimbus.compose.core.ui.NimbusServerDrivenView
import br.zup.com.nimbus.compose.model.NimbusPageState
import br.zup.com.nimbus.compose.model.Page

@Composable
internal fun NimbusView(
    page: Page,
    nimbusViewModel: NimbusViewModel,
    onStart: () -> Unit = {},
    onCreate: () -> Unit = {},
    onDestroy: () -> Unit = {},
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> onStart()
                Lifecycle.Event.ON_CREATE -> onCreate()
                Lifecycle.Event.ON_DESTROY -> onDestroy()
            }

        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            page.view.destroy()
        }
    }

    page.content?.let {
        NimbusBackHandler(nimbusViewModel)
        when (it) {
            is NimbusPageState.PageStateOnLoading -> {
                NimbusTheme.nimbusAppState.config.loadingView()
            }
            is NimbusPageState.PageStateOnError -> {
                NimbusTheme.nimbusAppState.config.errorView(it.throwable)
            }
            is NimbusPageState.PageStateOnShowPage -> {
                NimbusServerDrivenView(viewTree = it.serverDrivenNode)
            }
        }
    }
}