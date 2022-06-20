package br.zup.com.nimbus.compose.core.ui.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import br.zup.com.nimbus.compose.CoroutineDispatcherLib
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Composable to collect values from StateFlow
 */
@Composable
internal fun <T> CollectSharedFlow(
    sharedFlow: SharedFlow<T>,
    startDispatcher: CoroutineDispatcher = CoroutineDispatcherLib.backgroundPool,
    endDispatcher: CoroutineDispatcher = CoroutineDispatcherLib.mainThread,
    calculation: (T) -> Unit
) {
    LaunchedEffect(key1 = Unit) {
        this.launch(startDispatcher) {
            sharedFlow.collect { collectedValue ->
                withContext(endDispatcher) {
                    calculation(collectedValue)
                }
            }
        }
    }
}
