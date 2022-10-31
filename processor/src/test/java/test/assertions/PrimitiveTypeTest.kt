package test.assertions

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import test.utils.CompilationResult
import test.utils.compile
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("When action handlers with primitive types are deserialized")
class PrimitiveTypeTest {
    lateinit var compilation: CompilationResult

    @BeforeAll
    fun setup(@TempDir tempDir: File) {
        compilation = compile(
            """
            @AutoDeserialize
            fun required(
              name: String,
              age: Int,
              birthYear: Long,
              isUnderAge: Boolean,
              balance: Double,
              schoolGrade: Float,
            ) {
                TestResult.add(name, age, birthYear, isUnderAge, balance, schoolGrade)
            }
            
            @AutoDeserialize
            fun nullable(
              name: String?,
              age: Int?,
              birthYear: Long?,
              isUnderAge: Boolean?,
              balance: Double?,
              schoolGrade: Float?,
            ) {
                TestResult.add(name, age, birthYear, isUnderAge, balance, schoolGrade)
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

    @Nested
    @DisplayName("When all properties are given")
    inner class AllPropertiesGiven {
        private val properties = mapOf(
            "name" to "John Smith",
            "age" to 19,
            "birthYear" to 2003,
            "isUnderAge" to false,
            "balance" to 1235.27,
            "schoolGrade" to 72.95,
            "extraProperty" to "shouldMakeNoDifference",
        )

        private fun testDeserializationOf(functionName: String) {
            compilation.runEventForActionHandler(functionName, properties)
            compilation.assertResults("John Smith", 19, 2003L, false, 1235.27, 72.95F)
        }

        @Test
        fun `Then it should deserialize the required properties`() =
            testDeserializationOf("required")

        @Test
        fun `Then it should deserialize the nullable properties`() =
            testDeserializationOf("nullable")
    }

    @Nested
    @DisplayName("When some properties are missing")
    inner class AllPropertiesMissing {
        private val properties = mapOf(
            "age" to 21,
            "birthYear" to 2001,
        )

        @Test
        fun `Then it should fail to deserialize the required properties`() {
            val errors = listOf(
                "Expected a string for property \"name\", but found null",
                "Expected a boolean for property \"isUnderAge\", but found null",
                "Expected a number for property \"balance\", but found null",
                "Expected a number for property \"schoolGrade\", but found null",
            )
            compilation.runEventForActionHandlerCatching("required", properties, errors)
        }

        @Test
        fun `Then it should deserialize the nullable properties`() {
            compilation.runEventForActionHandler("nullable", properties)
            compilation.assertResults(null, 21, 2001L, null, null, null)
        }
    }
}
