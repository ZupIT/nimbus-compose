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
