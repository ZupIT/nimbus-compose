package test.assertions.runtime.annotation

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import test.BaseTest
import test.compiler.CompilationResult
import test.compiler.TestCompiler
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("When we use @Ignore on a parameter")
class IgnoreTest: BaseTest() {
    @BeforeAll
    fun setup(compiler: TestCompiler) {
        compilation = compiler.compile(
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
        compilation.assertOk()
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
