package test.assertions.build.error

import org.junit.jupiter.api.Test
import test.BaseTest
import test.compiler.TestCompiler

class UnsupportedFunctionTest: BaseTest() {
    fun test(type: String, implementation: String = "{}") {
        compilation = TestCompiler.compile("""
            @AutoDeserialize
            fun actionOrOperation(test: $type) $implementation
        """)
        compilation.assertProcessorError("UnsupportedFunction")
    }

    @Test
    fun `When the value of a Map is a function, it should raise a compilation error`() =
        test("Map<String, () -> Unit>")

    @Test
    fun `When the item of a List is a function, it should raise a compilation error`() =
        test("List<() -> Unit>")

    @Test
    fun `When a function is used in an Operation, it should raise a compilation error`() =
        test("() -> Unit", "= 0")
}