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

    @Test
    fun `When @Root is used on a custom deserialized type, it should raise a compilation error`() {
        compilation = TestCompiler.compile("""
            import br.com.zup.nimbus.annotation.Root
            
            class Animal {
                var name: String
            }
            
            @Deserializer
            fun deserializeAnimal(data: AnyServerDrivenData): Animal {}
            
            @AutoDeserialize
            fun rootWithCustomDeserialized(@Root val props: Animal?)
        """)
        compilation.assertProcessorError("InvalidUseOfRoot")
    }
}