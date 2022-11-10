package test.assertions.build.error

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import test.BaseTest
import test.compiler.TestCompiler

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("When we use a custom deserializer that can return null on a required property")
class DeserializerPossiblyNullTest: BaseTest() {
    @Test
    fun `should raise a compilation error`(compiler: TestCompiler) {
        compilation = compiler.compile("""
            class MyClass(val test: String)
            
            @Deserializer
            fun deserializeMyClass(data: AnyServerDrivenData): MyClass? {
                return if (data.isNull()) null else MyClass(data.asString())
            }
            
            @AutoDeserialize
            fun myAction(value: MyClass) {}
        """)
        compilation.assertProcessorError("DeserializerPossiblyNull: The parameter value of type " +
                "MyClass expects a non-nullable value, but its deserializer may return null.")
    }
}
