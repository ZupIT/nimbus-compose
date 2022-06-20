package br.zup.com.nimbus.compose.core.ui.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import br.zup.com.nimbus.compose.CoroutineDispatcherLib
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

/**
 * Composable to collect values from StateFlow
 */
@Composable
internal fun <T> CollectStateFlow(
    stateFlow: StateFlow<T>,
    startDispatcher: CoroutineDispatcher = CoroutineDispatcherLib.backgroundPool,
    endDispatcher: CoroutineDispatcher = CoroutineDispatcherLib.mainThread,
    calculation: (T) -> Unit
) {
    LaunchedEffect(key1 = Unit) {
        withContext(startDispatcher) {
            stateFlow.collect { collectedValue ->
                withContext(endDispatcher) {
                    calculation(collectedValue)
                }
            }
        }
    }
}
