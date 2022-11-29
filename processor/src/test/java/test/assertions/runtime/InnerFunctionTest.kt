package test.assertions.runtime

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import test.BaseRuntimeTest
import test.compiler.TestCompiler
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("When we use @AutoDeserializable for functions inside classes or objects")
class InnerFunctionTest: BaseRuntimeTest() {
    private fun sourceCode(isObject: Boolean, addCompanion: Boolean = false): String {
        val declaration = if(isObject) "object MyTest" else "class MyTest"
        val instance = if(isObject || addCompanion) "MyTest" else "MyTest()"

        fun createContent(content: String) =
            if (addCompanion) """
                companion object {
                    $content
                }
            """
            else content

        return """
            import br.com.zup.nimbus.processor.ComponentData
            import test.utils.MockNode
            import test.utils.MockAction
            import test.utils.MockEvent
            import com.zup.nimbus.core.ActionTriggeredEvent
            
            $declaration {
                ${createContent("""
                    @AutoDeserialize
                    @Composable
                    fun Component(value: String) {}
                    
                    @AutoDeserialize
                    fun action(value: String) {}
                    
                    @AutoDeserialize
                    fun operation(value: String) = 0
                """)}
            }
            
            /* the code below will make the compilation fail in case the functions above annotated
            with @AutoDeserializable are not generated as extensions of the class/object MyTest. */
            
            @Composable
            fun TestComponent() {
                $instance.Component(ComponentData(MockNode(), @Composable {}))
            }
            
            fun testAction() {
                $instance.action(ActionTriggeredEvent(MockAction(), MockEvent(emptyList()), mutableSetOf()))
            }
            
            fun testOperation() {
                $instance.operation(emptyList())
            }
        """
    }

    @Test
    fun `should declare components as extensions of object MyTest`() {
        TestCompiler.compile(sourceCode(true)).assertOk()
    }

    @Test
    fun `should declare components as extensions of class MyTest`() {
        TestCompiler.compile(sourceCode(false)).assertOk()
    }

    @Test
    fun `should declare components as extensions of the companion object of the class MyTest`() {
        TestCompiler.compile(sourceCode(isObject = false, addCompanion = true)).assertOk()
    }
}