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

package test.assertions.runtime.category

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import test.BaseRuntimeTest
import test.utils.DEFAULT_EVENT_NAME
import test.compiler.TestCompiler

/**
 * Most scenarios for action deserialization have already been tested by
 * "test.assertions.runtime.type". Here we test just some particularities of an action
 * deserialization that have not been tested yet.
 */
@DisplayName("When an action is deserialized")
class ActionTest: BaseRuntimeTest() {
    @BeforeAll
    fun setup() {
        compilation = TestCompiler.compile(
            """
                class ActionData(
                    val context: DeserializationContext,
                    val content: @Composable () -> Unit,
                )
                
                @AutoDeserialize
                fun myAction(data: ActionData) {
                    TestResult.add(
                        data.context.component?.node?.id,
                        data.context.event?.scope?.name,
                        data.content(),
                    )
                }
            """,
        )
    }

    @Test
    @DisplayName("When an action indirectly requests the deserialization context or the " +
            "component's children, it should receive null for the component data and the event " +
            "for event data")
    fun `should inject empty context and empty component to operation`() {
        compilation.runEventForActionHandler("myAction", emptyMap())
        compilation.assertResults(null, DEFAULT_EVENT_NAME, Unit)
    }
}