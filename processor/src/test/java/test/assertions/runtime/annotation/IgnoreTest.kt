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

package test.assertions.runtime.annotation

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import test.BaseRuntimeTest
import test.compiler.TestCompiler

@DisplayName("When we use @Ignore on a parameter")
class IgnoreTest: BaseRuntimeTest() {
    @BeforeAll
    fun setup() {
        compilation = TestCompiler.compile(
            """
                import br.com.zup.nimbus.annotation.Ignore
                import br.com.zup.nimbus.annotation.Alias
                
                class Person(
                    @Alias("fullName") @Ignore val name: String = "",
                    val age: Int,
                    @Ignore val birthDate: Long = 0L,
                )
                
                @AutoDeserialize
                fun actionWithIgnoredParams(
                    person: Person,
                    @Ignore friends: List<Person>? = null,
                    @Ignore notifications: Int = 0,
                    lastLogin: Long,
                    @Ignore context: DeserializationContext = DeserializationContext(),
                ) {
                    TestResult.add(
                        person.name,
                        person.age,
                        person.birthDate,
                        friends?.getOrNull(0),
                        notifications,
                        lastLogin,
                        context.component,
                        context.event,
                    )
                }
            """,
        )
    }

    @Test
    @DisplayName("Then all parameters annotated with @Ignore should be ignored And parameters" +
            "also annotated with @Alias should suffer no effect from the alias.")
    fun `should deserialize using the aliases`() {
        val properties = mapOf(
            "person" to mapOf(
                "fullName" to "Peter Griffin",
                "name" to "Nameless",
                "age" to 53,
            ),
            "friends" to listOf(5, 10, "test"),
            "lastLogin" to 80L,
        )
        compilation.runEventForActionHandler("actionWithIgnoredParams", properties)
        compilation.assertResults(
            "",
            53,
            0L,
            null,
            0,
            80L,
            null,
            null,
        )
    }
}
