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

package test

import androidx.compose.material.printedByTextComponent
import br.com.zup.nimbus.compose.MockLogger
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import test.compiler.CompilationResult
import test.extensions.ErrorWatcher
import test.extensions.PrepareAndClose

@ExtendWith(ErrorWatcher::class)
@ExtendWith(PrepareAndClose::class)
open class BaseTest {
    private var _compilation: CompilationResult? = null
    var compilation: CompilationResult
        get() = checkNotNull(_compilation) { "Must set compilation before requiring it" }
        set(result) { _compilation = result }

    fun checkCompilation() {
        _compilation?.assertOk()
    }

    @BeforeEach
    open fun clear() {
        _compilation?.clearResults()
        MockLogger.clear()
        printedByTextComponent.clear()
    }
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class BaseRuntimeTest: BaseTest() {
    @BeforeEach
    fun beforeEach() = checkCompilation()
}
