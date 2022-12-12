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

class InvalidFunctionTest: BaseTest() {
    fun test(signature: String) {
        compilation = TestCompiler.compile("""
            class MyClass(val test: String)
            
            @AutoDeserialize
            fun myAction(onPress: $signature) {}
        """)
        compilation.assertProcessorError("InvalidFunction")
    }

    // Unsupported types are anything other than: String, Boolean, Int, Long, Float, Double, Map
    // and List
    @Test
    fun `When a function parameter is of unsupported type, it should raise a compilation error`() =
        test("(MyClass) -> Unit")

    @Test
    fun `When a function has more than one parameter, it should raise a compilation error`() =
        test("(String, Int) -> Unit")

    @Test
    fun `When a function returns something other than Unit, it should raise a compilation error`() =
        test("() -> String")
}
