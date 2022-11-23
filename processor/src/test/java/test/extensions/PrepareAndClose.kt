package test.extensions

import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import test.compiler.TestCompiler
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class PrepareAndClose: BeforeAllCallback {
    override fun beforeAll(context: ExtensionContext) {
        if (!started) {
            started = true
            TestCompiler.clearCompilationDirectory()
            context.getStore(ExtensionContext.Namespace.GLOBAL).put("close", CloseTests())
        }
    }

    private class CloseTests : ExtensionContext.Store.CloseableResource {
        override fun close() {
            if (!hasFailingTests) TestCompiler.clearCompilationDirectory()
        }
    }

    companion object {
        private var started = false
        var hasFailingTests = false
    }
}
