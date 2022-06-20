package br.zup.com.nimbus.compose

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow

internal object CoroutineDispatcherLib {
    val backgroundPool = Dispatchers.Default
    val inputOutputPool = Dispatchers.IO
    val mainThread = Dispatchers.Main
    const val REPLAY_COUNT = 5
    val ON_BUFFER_OVERFLOW = BufferOverflow.DROP_OLDEST
}
