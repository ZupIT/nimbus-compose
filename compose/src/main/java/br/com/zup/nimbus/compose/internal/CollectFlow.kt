package br.com.zup.nimbus.compose.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import br.com.zup.nimbus.compose.CoroutineDispatcherLib
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Composable to collect values from StateFlow
 */
@Composable
internal fun <T> CollectFlow(
    flow: SharedFlow<T>,
    startDispatcher: CoroutineDispatcher = CoroutineDispatcherLib.backgroundPool,
    endDispatcher: CoroutineDispatcher = CoroutineDispatcherLib.mainThread,
    calculation: (T) -> Unit
) {
    LaunchedEffect(key1 = Unit) {
        this.launch(startDispatcher) {
            flow.collectLatest { collectedValue ->
                withContext(endDispatcher) {
                    calculation(collectedValue)
                }
            }
        }
    }
}
