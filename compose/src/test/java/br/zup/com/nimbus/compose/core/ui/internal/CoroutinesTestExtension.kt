package br.zup.com.nimbus.compose.core.ui.internal

import br.zup.com.nimbus.compose.CoroutineDispatcherLib
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

@ExperimentalCoroutinesApi
class CoroutinesTestExtension
constructor(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher(
        TestCoroutineScheduler())
) : BeforeEachCallback, AfterEachCallback {

    override fun beforeEach(context: ExtensionContext?) {
        CoroutineDispatcherLib.mainThread = Dispatchers.Unconfined
        CoroutineDispatcherLib.inputOutputPool = Dispatchers.Unconfined
        CoroutineDispatcherLib.backgroundPool = Dispatchers.Unconfined
        Dispatchers.setMain(dispatcher)
    }

    override fun afterEach(context: ExtensionContext?) {
        Dispatchers.resetMain()
        CoroutineDispatcherLib.reset()
    }
}