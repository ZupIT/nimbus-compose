package test.assertions.build.error

import org.junit.jupiter.api.Test
import test.BaseTest
import test.compiler.TestCompiler

class InvalidUseOfRootTest: BaseTest() {
    fun test(type: String) {
        compilation = TestCompiler.compile("""
            import br.com.zup.nimbus.annotation.Root
            
            @AutoDeserialize
            fun myAction(@Root test: $type) {}
        """)
        compilation.assertProcessorError("InvalidUseOfRoot")
    }

    @Test
    fun `When @Root is used on a String, it should raise a compilation error`() =
        test("String")

    @Test
    fun `When @Root is used on an Int, it should raise a compilation error`() =
        test("Int")

    @Test
    fun `When @Root is used on a Long, it should raise a compilation error`() =
        test("Long")

    @Test
    fun `When @Root is used on a Float, it should raise a compilation error`() =
        test("Float")

    @Test
    fun `When @Root is used on a Double, it should raise a compilation error`() =
        test("Double")
    @Test
    fun `When @Root is used on a Map, it should raise a compilation error`() =
        test("Map<String, String>")

    @Test
    fun `When @Root is used on a List, it should raise a compilation error`() =
        test("List<String>")

    @Test
    fun `When @Root is used in an Operation, it should raise a compilation error`() {
        compilation = TestCompiler.compile("""
            import br.com.zup.nimbus.annotation.Root
            
            class MyClass(val test: String)
            
            @AutoDeserialize
            fun myOperation(@Root test: MyClass) = 0
        """)
        compilation.assertProcessorError("InvalidUseOfRoot")
    }
}