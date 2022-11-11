package test.assertions.build.error

import org.junit.jupiter.api.Test
import test.BaseTest
import test.compiler.TestCompiler

class InvalidUseOfVarargTest: BaseTest() {
    fun test(annotation: String) {
        compilation = TestCompiler.compile("""
            @AutoDeserialize
            $annotation
            fun actionOrComponent(vararg values: String) {}
        """)
        compilation.assertProcessorError("InvalidUseOfVararg")
    }

    @Test
    fun `When vararg is used in an Action Handler, it should raise a compilation error`() =
        test("")

    @Test
    fun `When vararg is used in a Component, it should raise a compilation error`() =
        test("@Composable")
}