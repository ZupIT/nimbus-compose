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

class InvalidUseOfVarargTest: BaseTest() {
    fun test(annotation: String) {
        compilation = TestCompiler.compile("""
            @AutoDeserialize
            $annotation
            fun actionOrComponent(vararg values: String) {}
        """)
        compilation.assertProcessorError("InvalidUseOfVararg")
    }

    @Test
    fun `When vararg is used in an Action Handler, it should raise a compilation error`() =
        test("")

    @Test
    fun `When vararg is used in a Component, it should raise a compilation error`() =
        test("@Composable")
}