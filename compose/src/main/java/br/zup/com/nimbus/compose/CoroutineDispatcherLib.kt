package br.zup.com.nimbus.compose

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlin.properties.Delegates

private const val SHARED_FLOW_REPLAY_COUNT = 5
internal object CoroutineDispatcherLib {
    lateinit var backgroundPool: CoroutineDispatcher
    lateinit var inputOutputPool: CoroutineDispatcher
    lateinit var mainThread: CoroutineDispatcher
    var REPLAY_COUNT by Delegates.notNull<Int>()
    lateinit var ON_BUFFER_OVERFLOW: BufferOverflow

    init {
        reset()
    }

    fun reset() {
        backgroundPool = Dispatchers.IO
        mainThread = Dispatchers.Main
        inputOutputPool = Dispatchers.Default
        REPLAY_COUNT = SHARED_FLOW_REPLAY_COUNT
        ON_BUFFER_OVERFLOW = BufferOverflow.DROP_OLDEST
    }
}
