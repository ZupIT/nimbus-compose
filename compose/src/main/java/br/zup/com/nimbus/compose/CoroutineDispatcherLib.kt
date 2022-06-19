package br.zup.com.nimbus.compose

import kotlinx.coroutines.Dispatchers

internal object CoroutineDispatcherLib {
    val backgroundPool = Dispatchers.Default
    val inputOutputPool = Dispatchers.IO
    val mainThread = Dispatchers.Main
}
