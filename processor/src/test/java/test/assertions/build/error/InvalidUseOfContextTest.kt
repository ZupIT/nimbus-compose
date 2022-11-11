package test.assertions.build.error

import org.junit.jupiter.api.Test
import test.BaseTest
import test.compiler.TestCompiler

class InvalidUseOfContextTest: BaseTest() {
    @Test
    fun `When an Operation requests the DeserializationContext, it should raise a compilation error`() {
        compilation = TestCompiler.compile("""
            @AutoDeserialize
            fun myOperation(ctx: DeserializationContext) = 0
        """)
        compilation.assertProcessorError("InvalidUseOfContext")
    }
}