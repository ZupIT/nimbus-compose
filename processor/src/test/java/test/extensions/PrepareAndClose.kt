package test.extensions

import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import test.compiler.TestCompiler
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class PrepareAndClose: BeforeAllCallback {
    override fun beforeAll(context: ExtensionContext) {
        // lock the access so only one Thread has access to it
        LOCK.lock()
        try {
            if (!started) {
                started = true
                TestCompiler.clearCompilationDirectory()
                context.getStore(ExtensionContext.Namespace.GLOBAL).put("close", CloseTests())
            }
        } finally {
            // free the access
            LOCK.unlock()
        }
    }

    private class CloseTests : ExtensionContext.Store.CloseableResource {
        override fun close() {
            if (!hasFailingTests) TestCompiler.clearCompilationDirectory()
        }
    }

    companion object {
        /** Gate keeper to prevent multiple Threads within the same routine  */
        private val LOCK: Lock = ReentrantLock()

        /** volatile boolean to tell other threads, when unblocked, whether they should try attempt
         * start-up. Alternatively, could use AtomicBoolean.  */
        @Volatile
        private var started = false
        var hasFailingTests = false
    }
}
