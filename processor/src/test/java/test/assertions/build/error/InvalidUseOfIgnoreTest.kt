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

class InvalidUseOfIgnoreTest: BaseTest() {
    @Test
    fun `When @Ignore is used on a parameter that doesn't have a default value, it should raise a compilation error`() {
        compilation = TestCompiler.compile("""
            import br.com.zup.nimbus.annotation.Ignore
            
            @AutoDeserialize
            fun myAction(@Ignore test: String?) {}
        """)
        compilation.assertProcessorError("InvalidUseOfIgnore")
    }
}