package test.assertions.build.error

import org.junit.jupiter.api.Test
import test.BaseTest
import test.compiler.TestCompiler

class InvalidMapTypeTest: BaseTest() {
    @Test
    fun `When the key type of a map is not String, it should raise a compilation error`() {
        compilation = TestCompiler.compile("""
            @AutoDeserialize
            fun myAction(map: Map<Int, String>) {}
        """)
        compilation.assertProcessorError("InvalidMapType")
    }
}
