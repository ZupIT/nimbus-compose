package test

import androidx.compose.material.printedByTextComponent
import br.zup.com.nimbus.compose.MockLogger
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import test.compiler.CompilationResult
import test.compiler.CompilerResolver
import test.extensions.ErrorWatcher
import test.extensions.PrepareAndClose

@ExtendWith(CompilerResolver::class)
@ExtendWith(ErrorWatcher::class)
@ExtendWith(PrepareAndClose::class)
open class BaseTest {
    private var _compilation: CompilationResult? = null
    var compilation: CompilationResult
        get() = _compilation ?: throw IllegalStateException("Must set compilation before requiring it")
        set(result) { _compilation = result }

    @BeforeEach
    open fun clear() {
        _compilation?.clearResults()
        MockLogger.clear()
        printedByTextComponent.clear()
    }
}
