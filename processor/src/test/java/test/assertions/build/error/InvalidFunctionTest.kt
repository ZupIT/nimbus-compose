package test.assertions.build.error

import org.junit.jupiter.api.Test
import test.BaseTest
import test.compiler.TestCompiler

class InvalidFunctionTest: BaseTest() {
    fun test(signature: String) {
        compilation = TestCompiler.compile("""
            class MyClass(val test: String)
            
            @AutoDeserialize
            fun myAction(onPress: $signature) {}
        """)
        compilation.assertProcessorError("InvalidFunction")
    }

    // Unsupported types are anything other than: String, Boolean, Int, Long, Float, Double, Map
    // and List
    @Test
    fun `When a function parameter is of unsupported type, it should raise a compilation error`() =
        test("(MyClass) -> Unit")

    @Test
    fun `When a function has more than one parameter, it should raise a compilation error`() =
        test("(String, Int) -> Unit")

    @Test
    fun `When a function returns something other than Unit, it should raise a compilation error`() =
        test("() -> String")
}
