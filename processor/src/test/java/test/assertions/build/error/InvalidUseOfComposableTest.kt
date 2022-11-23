package test.assertions.build.error

import org.junit.jupiter.api.Test
import test.BaseTest
import test.compiler.TestCompiler

class InvalidUseOfComposableTest: BaseTest() {
    fun test(implementation: String) {
        compilation = TestCompiler.compile("""
            @AutoDeserialize
            fun actionOrOperation(component: @Composable () -> Unit) $implementation
        """)
        compilation.assertProcessorError("InvalidUseOfComposable")
    }

    @Test
    fun `When a Composable is used as a parameter of an Action Handler, it should raise a compilation error`() =
        test("{}")

    @Test
    fun `When a Composable is used as a parameter of an Operation, it should raise a compilation error`() =
        test("= 0")
}