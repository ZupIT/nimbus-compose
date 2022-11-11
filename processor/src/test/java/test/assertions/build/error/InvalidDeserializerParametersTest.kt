package test.assertions.build.error

import org.junit.jupiter.api.Test
import test.BaseTest
import test.compiler.TestCompiler

class InvalidDeserializerParametersTest: BaseTest() {
    fun test(parameters: String) {
        compilation = TestCompiler.compile("""
            class MyClass(val test: String)
            
            @Deserializer
            fun deserializeMyClass($parameters): MyClass? {
                return if (data.isNull()) null else MyClass(data.asString())
            }
        """)
        compilation.assertProcessorError("InvalidDeserializerParameters")
    }

    @Test
    fun `When no parameters are given to a deserializer, it should raise a compilation error`() =
        test("")

    @Test
    fun `When the first parameter of a deserializer is of wrong type, it should raise a compilation error`() =
        test("data: String")

    @Test
    fun `When the second parameter of a deserializer is of wrong type, it should raise a compilation error`() =
        test("data: AnyServerDrivenData, ctx: Int")

    @Test
    fun `When the deserializer has more than 2 parameters, it should raise a compilation error`() =
        test("data: AnyServerDrivenData, ctx: DeserializationContext, extra: String")
}