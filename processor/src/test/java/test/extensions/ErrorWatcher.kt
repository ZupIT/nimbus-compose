package test.extensions

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestWatcher
import test.compiler.COMPILATION_OUTPUT_KEY
import test.compiler.TestCompiler
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

class ErrorWatcher: TestWatcher {
    override fun testFailed(extensionContext: ExtensionContext?, throwable: Throwable?) {
//        extensionContext?.let {
//            val store = it.getStore(ExtensionContext.Namespace.GLOBAL)
//            val fullKey = TestCompiler.getStoreKey(it.testClass.get(), it.testMethod.get())
//            val classOnlyKey = TestCompiler.getStoreKey(it.testClass.get())
//            val output = store.get(fullKey) ?: store.get(classOnlyKey) as? ByteArrayOutputStream
//            output?.let { stream -> println(stream) }
//        }

        PrepareAndClose.hasFailingTests = true
    }
}
