package test.assertions.build.error

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import test.BaseTest
import test.compiler.TestCompiler

@DisplayName("When we create cyclic references using the annotation @Root")
class RootCycleErrorTest: BaseTest() {
    @Test
    fun `should raise a compilation error`() {
        compilation = TestCompiler.compile("""
            import br.com.zup.nimbus.annotation.Root
            
            data class CyclicClassA(val b: String, val c: Int, @Root val d: CyclicClassB) {}
            data class CyclicClassB(@Root val e: CyclicClassC, val f: Boolean) {}
            data class CyclicClassC(val g: Int, @Root val h: CyclicClassA?) {}
            
            @AutoDeserialize
            fun cyclicAction(@Root a: CyclicClassA) {}
        """)
        compilation.assertProcessorError(
            "RootCycleError",
            "Cyclic reference found in the constructor of CyclicClassC at the parameter named h " +
                    "of type CyclicClassA"
        )
    }
}
