package test

import androidx.compose.material.printedByTextComponent
import br.com.zup.nimbus.processor.MockLogger
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import test.compiler.CompilationResult
import test.extensions.ErrorWatcher
import test.extensions.PrepareAndClose

@ExtendWith(ErrorWatcher::class)
@ExtendWith(PrepareAndClose::class)
open class BaseTest {
    private var _compilation: CompilationResult? = null
    var compilation: CompilationResult
        get() = checkNotNull(_compilation) { "Must set compilation before requiring it" }
        set(result) { _compilation = result }

    fun checkCompilation() {
        _compilation?.assertOk()
    }

    @BeforeEach
    open fun clear() {
        _compilation?.clearResults()
        MockLogger.clear()
        printedByTextComponent.clear()
    }
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class BaseRuntimeTest: BaseTest() {
    @BeforeEach
    fun beforeEach() = checkCompilation()
}
