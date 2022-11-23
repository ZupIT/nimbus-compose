package test.assertions.runtime.type

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import test.BaseRuntimeTest
import test.compiler.TestCompiler

@DisplayName("When action handlers with primitive types are deserialized")
class PrimitiveTest: BaseRuntimeTest() {
    @BeforeAll
    fun setup() {
        compilation = TestCompiler.compile(
            """
                @AutoDeserialize
                fun required(
                  name: String,
                  age: Int,
                  birthYear: Long,
                  isUnderAge: Boolean,
                  balance: Double,
                  schoolGrade: Float,
                  metadata: Any,
                ) {
                    TestResult.add(name, age, birthYear, isUnderAge, balance, schoolGrade, metadata)
                }
                
                @AutoDeserialize
                fun nullable(
                  name: String?,
                  age: Int?,
                  birthYear: Long?,
                  isUnderAge: Boolean?,
                  balance: Double?,
                  schoolGrade: Float?,
                  metadata: Any?,
                ) {
                    TestResult.add(name, age, birthYear, isUnderAge, balance, schoolGrade, metadata)
                }
            """,
        )
    }

    @Nested
    @DisplayName("When all properties are given correctly")
    inner class AllPropertiesGivenCorrectly {
        private val metadata = mapOf("nickname" to "JS", "description" to "A nice guy")
        private val properties = mapOf(
            "name" to "John Smith",
            "age" to 19,
            "birthYear" to 2003L,
            "isUnderAge" to false,
            "balance" to 1235.27,
            "schoolGrade" to 72.95F,
            "metadata" to metadata,
            "extraProperty" to "shouldMakeNoDifference",
        )

        private fun testDeserializationOf(functionName: String) {
            compilation.runEventForActionHandler(functionName, properties)
            compilation.assertResults("John Smith", 19, 2003L, false, 1235.27, 72.95F, metadata)
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
    inner class SomePropertiesMissing {
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
                "Expected anything for property \"metadata\", but found null",
            )
            compilation.runEventForActionHandlerCatching("required", properties, errors)
        }

        @Test
        fun `Then it should deserialize the nullable properties`() {
            compilation.runEventForActionHandler("nullable", properties)
            compilation.assertResults(null, 21, 2001L, null, null, null, null)
        }
    }

    @Nested
    @DisplayName("When some properties are invalid")
    inner class SomePropertiesInvalid {
        private val properties = mapOf(
            "name" to "John Smith",
            "age" to "underage",
            "birthYear" to "unknown",
            "isUnderAge" to 34,
            "balance" to false,
            "schoolGrade" to true,
            "metadata" to "",
        )

        private fun testFailureOf(functionName: String) {
            val errors = listOf(
                "Expected a number for property \"age\", but found String",
                "Expected a number for property \"birthYear\", but found String",
                "Expected a boolean for property \"isUnderAge\", but found Int",
                "Expected a number for property \"balance\", but found Boolean",
                "Expected a number for property \"schoolGrade\", but found Boolean",
            )
            compilation.runEventForActionHandlerCatching(functionName, properties, errors)
        }

        @Test
        fun `Then it should fail to deserialize the required properties`() = testFailureOf("required")

        @Test
        fun `Then it should deserialize the nullable properties`() = testFailureOf("nullable")
    }

    @Nested
    @DisplayName("When some properties are not the expected type, but can be coerced")
    inner class TypeCoercion {
        // No need to be exhaustive here, nimbus-core already tests the entity AnyServerDrivenData
        private val properties = mapOf(
            "name" to 790.13,
            "age" to 19F,
            "birthYear" to "2003",
            "isUnderAge" to false,
            "balance" to 32,
            "schoolGrade" to "20.62",
            "metadata" to "",
        )

        private fun testDeserializationOf(functionName: String) {
            compilation.runEventForActionHandler(functionName, properties)
            compilation.assertResults("790.13", 19, 2003L, false, 32.0, 20.62F, "")
        }

        @Test
        fun `Then it should deserialize the required properties`() =
            testDeserializationOf("required")

        @Test
        fun `Then it should deserialize the nullable properties`() =
            testDeserializationOf("nullable")
    }
}
