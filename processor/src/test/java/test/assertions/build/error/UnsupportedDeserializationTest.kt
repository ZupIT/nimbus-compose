/*
 * Copyright 2023 ZUP IT SERVICOS EM TECNOLOGIA E INOVACAO SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test.assertions.build.error

import org.junit.jupiter.api.Test
import test.BaseTest
import test.compiler.TestCompiler

private const val CLASS_NAME = "MyClass"

class UnsupportedDeserializationTest: BaseTest() {
    fun test(code: String, parameterType: String = CLASS_NAME) {
        compilation = TestCompiler.compile("""
            $code
            
            @AutoDeserialize
            fun myAction(test: $parameterType) {}
        """)
        compilation.assertProcessorError("UnsupportedDeserialization")
    }

    @Test
    fun `When we try to auto-deserialize a class with a parameter-less constructor, it should raise an error`() =
        test("class $CLASS_NAME {}")

    @Test
    fun `When we try to auto-deserialize a java class (no primary constructor), it should raise an error`() =
        test("import java.util.Date", "Date")

    @Test
    fun `When we try to auto-deserialize a sealed class, it should raise an error`() =
        test("""
            sealed class $CLASS_NAME(val test: Int) {
                object Value1: $CLASS_NAME(1)
                object Value2: $CLASS_NAME(2)
            }
        """)

    @Test
    fun `When we try to auto-deserialize a type alias to a class, it should raise an error`() =
        test(
            """
                class MyClassDeclaration(val test: Int)
                typealias $CLASS_NAME = MyClassDeclaration
            """,
        )

    @Test
    fun `When we try to auto-deserialize an abstract class, it should raise an error`() =
        test("abstract class $CLASS_NAME(val test: Int)")

    @Test
    fun `When we try to auto-deserialize an interface, it should raise an error`() =
        test("interface $CLASS_NAME { val test: Int }")

    @Test
    fun `When we try to auto-deserialize a generic class, it should raise an error`() =
        test("class $CLASS_NAME<T>(val test: T)", "$CLASS_NAME<T>")
}
