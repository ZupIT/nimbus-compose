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