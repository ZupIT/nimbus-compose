package test.assertions.build.error

import org.junit.jupiter.api.Test
import test.BaseTest
import test.compiler.TestCompiler

class InvalidUseOfIgnoreTest: BaseTest() {
    @Test
    fun `When @Ignore is used on a parameter that doesn't have a default value, it should raise a compilation error`() {
        compilation = TestCompiler.compile("""
            import br.com.zup.nimbus.annotation.Ignore
            
            @AutoDeserialize
            fun myAction(@Ignore test: String?) {}
        """)
        compilation.assertProcessorError("InvalidUseOfIgnore")
    }
}