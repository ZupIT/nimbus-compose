package test.assertions.runtime.annotation

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import test.utils.CompilationResult
import test.utils.DEFAULT_EVENT_NAME
import test.utils.compile
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("When we use @Alias on a parameter")
class AliasTest {
    lateinit var compilation: CompilationResult

    @BeforeAll
    fun setup(@TempDir tempDir: File) {
        compilation = compile(
            """
                import br.com.zup.nimbus.annotation.Alias
                import br.com.zup.nimbus.annotation.Root
                import br.com.zup.nimbus.annotation.Ignore
                
                class Person(
                    @Alias("fullName") val name: String,
                    val age: Int,
                    @Alias("birthDay") val birthDate: Long?,
                )
                
                @AutoDeserialize
                fun actionWithAliases(
                    @Alias("user") person: Person,
                    @Alias("connections") friends: List<Person>?,
                    @Alias("unreadNotificationsCount") notifications: Int,
                    lastLogin: Long,
                ) {
                    TestResult.add(
                        person.name,
                        person.age,
                        person.birthDate,
                        friends?.getOrNull(0)?.name,
                        friends?.getOrNull(0)?.age,
                        notifications,
                        lastLogin,
                    )
                }
            """,
            tempDir,
        )
        compilation.assertOk()
    }

    @BeforeEach
    fun clear() {
        compilation.clearResults()
    }

    @Test
    @DisplayName("Then the alias should be used to deserialize instead of the variable name")
    fun `should deserialize using the aliases`() {
        val properties = mapOf(
            "user" to mapOf(
                "fullName" to "Peter Griffin",
                "name" to "Nameless",
                "age" to 53,
                "birthDay" to 10L,
            ),
            "connections" to listOf(
                mapOf(
                    "fullName" to "Louis Griffin",
                    "age" to 49,
                )
            ),
            "unreadNotificationsCount" to 12,
            "lastLogin" to 80L,
        )
        compilation.runEventForActionHandler("actionWithAliases", properties)
        compilation.assertResults(
            "Peter Griffin",
            53,
            10L,
            "Louis Griffin",
            49,
            12,
            80L,
        )
    }
}
