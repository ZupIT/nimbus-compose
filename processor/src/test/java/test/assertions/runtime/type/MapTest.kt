package test.assertions.runtime.type

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import test.BaseTest
import test.compiler.CompilationResult
import test.utils.Snippets
import test.compiler.TestCompiler
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("When action handlers with map types are deserialized")
class MapTest: BaseTest() {
    private val cpfValue = "01444752174"
    private val myDateValue = 15L

    @BeforeAll
    fun setup() {
        compilation = TestCompiler.compile(
            """
                ${Snippets.documentAndDocumentType}
                ${Snippets.myDate}
                
                @AutoDeserialize
                fun required(
                  stringMap: Map<String, String>,
                  intMap: Map<String, Int>,
                  longMap: Map<String, Long>,
                  floatMap: Map<String, Float>,
                  doubleMap: Map<String, Double>,
                  booleanMap: Map<String, Boolean>,
                  anyMap: Map<String, Any>,
                  enumMap: Map<String, DocumentType>,
                  classMap: Map<String, Document>,
                  customClassMap: Map<String, MyDate>,
                  listMap: Map<String, List<Int>>,
                  stringMapMapMap: Map<String, Map<String, Map<String, String>>>,
                  stringMapMapMap2: Map<String, Map<String, Map<String, String>>>,
                ) {
                    TestResult.add(stringMap, intMap, longMap, floatMap, doubleMap,
                        booleanMap, anyMap, enumMap, classMap, customClassMap, listMap,
                        stringMapMapMap, stringMapMapMap2)
                }
                
                @AutoDeserialize
                fun nullableItems(
                  stringMap: Map<String, String?>,
                  intMap: Map<String, Int?>,
                  longMap: Map<String, Long?>,
                  floatMap: Map<String, Float?>,
                  doubleMap: Map<String, Double?>,
                  booleanMap: Map<String, Boolean?>,
                  anyMap: Map<String, Any?>,
                  enumMap: Map<String, DocumentType?>,
                  classMap: Map<String, Document?>,
                  customClassMap: Map<String, MyDate?>,
                  listMap: Map<String, List<Int>?>,
                  stringMapMapMap: Map<String, Map<String, Map<String, String>>?>,
                  stringMapMapMap2: Map<String, Map<String, Map<String, String?>>>,
                ) {
                    TestResult.add(stringMap, intMap, longMap, floatMap, doubleMap,
                        booleanMap, anyMap, enumMap, classMap, customClassMap, listMap,
                        stringMapMapMap, stringMapMapMap2)
                }
                
                @AutoDeserialize
                fun nullable(
                  stringMap: Map<String, String>?,
                  intMap: Map<String, Int>?,
                  longMap: Map<String, Long>?,
                  floatMap: Map<String, Float>?,
                  doubleMap: Map<String, Double>?,
                  booleanMap: Map<String, Boolean>?,
                  anyMap: Map<String, Any>?,
                  enumMap: Map<String, DocumentType>?,
                  classMap: Map<String, Document>?,
                  customClassMap: Map<String, MyDate>?,
                  listMap: Map<String, List<Int>>?,
                  stringMapMapMap: Map<String, Map<String, Map<String, String>>>?,
                  stringMapMapMap2: Map<String, Map<String, Map<String, String>>>?,
                ) {
                    TestResult.add(stringMap, intMap, longMap, floatMap, doubleMap,
                        booleanMap, anyMap, enumMap, classMap, customClassMap, listMap,
                        stringMapMapMap, stringMapMapMap2)
                }
            """,
            
        )
        compilation.assertOk()
    }

    private fun myDate(value: Long = myDateValue) = compilation.instanceOf("MyDate", value)
    private fun rg() = compilation.loadEnum("DocumentType", "RG")
    private fun cpf() = compilation.loadEnum("DocumentType", "CPF")
    private fun document() = compilation.instanceOf("Document", listOf(cpf(), cpfValue))

    @Nested
    @DisplayName("When all properties are given correctly")
    inner class AllPropertiesGiven {
        private val properties = mapOf(
            "stringMap" to mapOf("key" to "test"),
            "intMap" to mapOf("key" to 10),
            "longMap" to mapOf("key" to 12L),
            "floatMap" to mapOf("key" to 14.365F),
            "doubleMap" to mapOf("key" to 13.75),
            "booleanMap" to mapOf("key" to true, "key2" to false),
            "anyMap" to mapOf("key" to 5, "key2" to 10L, "key3" to "rg"),
            "enumMap" to mapOf("key" to "RG", "key2" to "CPF"),
            "classMap" to mapOf("key" to mapOf("type" to "CPF", "value" to cpfValue)),
            "customClassMap" to mapOf("key" to myDateValue),
            "listMap" to mapOf("key" to listOf(10)),
            "stringMapMapMap" to mapOf("key" to mapOf("key" to mapOf("key" to "test"))),
            "stringMapMapMap2" to mapOf("key" to mapOf("key" to mapOf("key" to "test"))),
        )

        private fun testDeserializationOf(functionName: String) {
            compilation.runEventForActionHandler(functionName, properties)
            compilation.assertResults(
                properties["stringMap"],
                properties["intMap"],
                properties["longMap"],
                properties["floatMap"],
                properties["doubleMap"],
                properties["booleanMap"],
                properties["anyMap"],
                mapOf("key" to rg(), "key2" to cpf()),
                mapOf("key" to document()),
                mapOf("key" to myDate()),
                properties["listMap"],
                properties["stringMapMapMap"],
                properties["stringMapMapMap2"],
            )
        }

        @DisplayName("Then it should deserialize action with required properties")
        @Test
        fun `should deserialize required action`() = testDeserializationOf("required")

        @DisplayName("Then it should deserialize action where the list items are optional")
        @Test
        fun `should deserialize optional items action`() =
            testDeserializationOf("nullableItems")

        @DisplayName("Then it should deserialize action with optional properties")
        @Test
        fun `should deserialize optional action`() = testDeserializationOf("nullable")
    }

    @Nested
    @DisplayName("When some properties have null map values")
    inner class SomeNullItems {
        private val properties = mapOf(
            "stringMap" to mapOf("key" to null, "key2" to null, "key3" to "test"),
            "intMap" to mapOf("key" to 10, "key2" to null),
            "longMap" to mapOf("key" to null, "key2" to 42L),
            "floatMap" to mapOf("key" to null, "key2" to 14.365F),
            "doubleMap" to mapOf("key" to null, "key2" to 13.75),
            "booleanMap" to mapOf("key" to null, "key2" to true),
            "anyMap" to mapOf("key" to 5, "key2" to null),
            "enumMap" to mapOf("key" to null, "key2" to "RG"),
            "classMap" to mapOf(
                "key" to null,
                "key2" to mapOf("type" to "CPF", "value" to cpfValue),
            ),
            "customClassMap" to mapOf(
                "key" to null,
                "key2" to myDateValue,
            ),
            "listMap" to mapOf("key" to listOf(2), "key2" to null),
            "stringMapMapMap" to mapOf("key" to null, "key2" to mapOf("key" to mapOf("key" to "test"))),
            "stringMapMapMap2" to mapOf("key" to mapOf("key" to mapOf("key" to null, "key2" to "test"))),
        )

        private fun testFailureOf(functionName: String) {
            val errors = listOf(
                "Expected a string for property \"stringMap.key\", but found null",
                "Expected a string for property \"stringMap.key2\", but found null",
                "Expected a number for property \"intMap.key2\", but found null",
                "Expected a number for property \"longMap.key\", but found null",
                "Expected a number for property \"floatMap.key\", but found null",
                "Expected a number for property \"doubleMap.key\", but found null",
                "Expected a boolean for property \"booleanMap.key\", but found null",
                "Expected CPF, RG, CNH for property \"enumMap.key\", but found null",
                "Expected CPF, RG, CNH for property \"classMap.key.type\", but found null",
                "Expected a string for property \"classMap.key.value\", but found null",
                "Expected an array for property \"listMap.key2\", but found null",
                "Expected an object for property \"stringMapMapMap.key\", but found null",
                "Expected a string for property \"stringMapMapMap2.key.key.key\", but found null"
            )
            compilation.runEventForActionHandlerCatching(functionName, properties, errors)
        }

        @DisplayName("Then it should fail to deserialize action with required properties")
        @Test
        fun `should fail to deserialize required action`() = testFailureOf("required")

        @DisplayName("Then it should deserialize action where the map values are optional")
        @Test
        fun `should deserialize optional values action`() {
            compilation.runEventForActionHandler("nullableItems", properties)
            compilation.assertResults(
                properties["stringMap"],
                properties["intMap"],
                properties["longMap"],
                properties["floatMap"],
                properties["doubleMap"],
                properties["booleanMap"],
                properties["anyMap"],
                mapOf("key" to null, "key2" to rg()),
                mapOf("key" to null, "key2" to document()),
                // ["key"] is not null because the custom deserializer is supposed to manage this.
                mapOf("key" to myDate(0L), "key2" to myDate()),
                properties["listMap"],
                properties["stringMapMapMap"],
                properties["stringMapMapMap2"],
            )
        }

        @DisplayName("Then it should fail to deserialize action with optional properties")
        @Test
        fun `should fail to deserialize optional action`() = testFailureOf("nullable")
    }

    @Nested
    @DisplayName("When some properties are null")
    inner class SomeNullProperties {
        private val properties = mapOf(
            "stringMap" to mapOf("key" to "test"),
            "floatMap" to mapOf("key" to 14.365F),
        )

        private fun testFailureOf(functionName: String) {
            val errors = listOf(
                "Expected an object for property \"intMap\", but found null",
                "Expected an object for property \"longMap\", but found null",
                "Expected an object for property \"doubleMap\", but found null",
                "Expected an object for property \"booleanMap\", but found null",
                "Expected an object for property \"anyMap\", but found null",
                "Expected an object for property \"enumMap\", but found null",
                "Expected an object for property \"classMap\", but found null",
                "Expected an object for property \"customClassMap\", but found null",
                "Expected an object for property \"listMap\", but found null",
                "Expected an object for property \"stringMapMapMap\", but found null",
                "Expected an object for property \"stringMapMapMap2\", but found null",
            )
            compilation.runEventForActionHandlerCatching(functionName, properties, errors)
        }

        @DisplayName("Then it should fail to deserialize action with required properties")
        @Test
        fun `should fail to deserialize required action`() = testFailureOf("required")

        @DisplayName("Then it should fail to deserialize action where the map values are optional")
        @Test
        fun `should fail to deserialize optional values action`() = testFailureOf("nullableItems")

        @DisplayName("Then it should deserialize action with optional properties")
        @Test
        fun `should deserialize optional action`() {
            compilation.runEventForActionHandler("nullable", properties)
            compilation.assertResults(
                properties["stringMap"],
                null,
                null,
                properties["floatMap"],
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
            )
        }
    }

    @Nested
    @DisplayName("When some properties are invalid")
    inner class SomeInvalidProperties {
        private val properties = mapOf(
            "stringMap" to "test",
            "intMap" to mapOf("key" to 10, "key2" to "test"),
            "longMap" to 47L,
            "floatMap" to mapOf("key" to 14.365F, "key2" to true),
            "doubleMap" to mapOf("key" to false),
            "booleanMap" to mapOf("key" to 10, "key2" to "test"),
            "anyMap" to 10,
            "enumMap" to mapOf("key" to "test"),
            "classMap" to mapOf("key" to 20),
            "customClassMap" to mapOf("key" to false),
            "listMap" to mapOf("key" to listOf(1), "key2" to 10),
            "stringMapMapMap" to mapOf("key" to mapOf("key" to 10)),
            "stringMapMapMap2" to mapOf("key" to true),
        )

        private fun testFailureOf(functionName: String) {
            val errors = listOf(
                "Expected an object for property \"stringMap\", but found String",
                "Expected a number for property \"intMap.key2\", but found String",
                "Expected an object for property \"longMap\", but found Long",
                "Expected a number for property \"floatMap.key2\", but found Boolean",
                "Expected a number for property \"doubleMap.key\", but found Boolean",
                "Expected a boolean for property \"booleanMap.key\", but found Int",
                "Expected a boolean for property \"booleanMap.key2\", but found String",
                "Expected an object for property \"anyMap\", but found Int",
                "Expected CPF, RG, CNH for property \"enumMap.key\", but found test",
                "Expected CPF, RG, CNH for property \"classMap.key.type\", but found null",
                "Expected a string for property \"classMap.key.value\", but found null",
                "Expected a number for property \"customClassMap.key\", but found Boolean",
                "Expected an array for property \"listMap.key2\", but found Int",
                "Expected an object for property \"stringMapMapMap.key.key\", but found Int",
                "Expected an object for property \"stringMapMapMap2.key\", but found Boolean",
            )
            compilation.runEventForActionHandlerCatching(functionName, properties, errors)
        }

        @DisplayName("Then it should fail to deserialize action with required properties")
        @Test
        fun `should fail to deserialize required action`() = testFailureOf("required")

        @DisplayName("Then it should fail to deserialize action where the list items are optional")
        @Test
        fun `should fail to deserialize optional items action`() = testFailureOf("nullableItems")

        @DisplayName("Then it should fail to deserialize action with optional properties")
        @Test
        fun `should fail to deserialize optional action`() = testFailureOf("nullable")
    }
}
