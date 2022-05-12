package br.zup.com.nimbus.compose.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.key
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import br.zup.com.nimbus.compose.NimbusTheme
import com.zup.nimbus.core.tree.ServerDrivenNode

@Composable
internal fun NimbusView(
    viewTree: ServerDrivenNode,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                onStart()
            }
        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
//                viewTree.dispose()
        }
    }

    if (!NimbusTheme.nimbusAppState.nimbusConfig.components.containsKey(viewTree.component)) {
        throw Error("Component with type ${viewTree.component} is not registered")
    }
    key(viewTree.id) {
        NimbusTheme.nimbusAppState.nimbusConfig.components[viewTree.component]!!(
            element = viewTree,
            children = {
                viewTree.children?.forEach {
                    NimbusView(it)
                }
            })
    }
}

fun onStart() {
    // TODO handle event
}

