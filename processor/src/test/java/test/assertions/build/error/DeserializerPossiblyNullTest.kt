package test.assertions.build.error

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import test.BaseTest
import test.compiler.TestCompiler

@DisplayName("When we use a custom deserializer that can return null on a required property")
class DeserializerPossiblyNullTest: BaseTest() {
    @Test
    fun `should raise a compilation error`() {
        compilation = TestCompiler.compile("""
            class MyClass(val test: String)
            
            @Deserializer
            fun deserializeMyClass(data: AnyServerDrivenData): MyClass? {
                return if (data.isNull()) null else MyClass(data.asString())
            }
            
            @AutoDeserialize
            fun myAction(value: MyClass) {}
        """)
        compilation.assertProcessorError("DeserializerPossiblyNull")
    }
}
