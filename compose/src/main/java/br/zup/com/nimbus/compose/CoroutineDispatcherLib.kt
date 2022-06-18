package br.zup.com.nimbus.compose

import kotlinx.coroutines.Dispatchers

object CoroutineDispatcherLib {
    var backgroundPool = Dispatchers.Default
    var mainThread = Dispatchers.Main
}
