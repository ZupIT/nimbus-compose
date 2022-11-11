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
@DisplayName("When action handlers with list types are deserialized")
class ListTest: BaseTest() {
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
                  stringList: List<String>,
                  intList: List<Int>,
                  longList: List<Long>,
                  floatList: List<Float>,
                  doubleList: List<Double>,
                  booleanList: List<Boolean>,
                  anyList: List<Any>,
                  enumList: List<DocumentType>,
                  classList: List<Document>,
                  customClassList: List<MyDate>,
                  mapList: List<Map<String, Int>>,
                  stringListListList: List<List<List<String>>>,
                  stringListListList2: List<List<List<String>>>,
                ) {
                    TestResult.add(stringList, intList, longList, floatList, doubleList,
                        booleanList, anyList, enumList, classList, customClassList, mapList,
                        stringListListList, stringListListList2)
                }
                
                @AutoDeserialize
                fun nullableItems(
                  stringList: List<String?>,
                  intList: List<Int?>,
                  longList: List<Long?>,
                  floatList: List<Float?>,
                  doubleList: List<Double?>,
                  booleanList: List<Boolean?>,
                  anyList: List<Any?>,
                  enumList: List<DocumentType?>,
                  classList: List<Document?>,
                  customClassList: List<MyDate?>,
                  mapList: List<Map<String, Int>?>,
                  stringListListList: List<List<List<String>>?>,
                  stringListListList2: List<List<List<String?>>>,
                ) {
                    TestResult.add(stringList, intList, longList, floatList, doubleList,
                        booleanList, anyList, enumList, classList, customClassList, mapList,
                        stringListListList, stringListListList2)
                }
                
                @AutoDeserialize
                fun nullable(
                  stringList: List<String>?,
                  intList: List<Int>?,
                  longList: List<Long>?,
                  floatList: List<Float>?,
                  doubleList: List<Double>?,
                  booleanList: List<Boolean>?,
                  anyList: List<Any>?,
                  enumList: List<DocumentType>?,
                  classList: List<Document>?,
                  customClassList: List<MyDate>?,
                  mapList: List<Map<String, Int>>?,
                  stringListListList: List<List<List<String>>>?,
                  stringListListList2: List<List<List<String>>>?,
                ) {
                    TestResult.add(stringList, intList, longList, floatList, doubleList,
                        booleanList, anyList, enumList, classList, customClassList, mapList,
                        stringListListList, stringListListList2)
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
            "stringList" to listOf("test"),
            "intList" to listOf(10),
            "longList" to listOf(12L),
            "floatList" to listOf(14.365F),
            "doubleList" to listOf(13.75),
            "booleanList" to listOf(true, false),
            "anyList" to listOf(5, 10L, "rg"),
            "enumList" to listOf("RG", "CPF"),
            "classList" to listOf(
                mapOf("type" to "CPF", "value" to cpfValue)
            ),
            "customClassList" to listOf(myDateValue),
            "mapList" to listOf(mapOf("value" to 10)),
            "stringListListList" to listOf(listOf(listOf("test"))),
            "stringListListList2" to listOf(listOf(listOf("test"))),
        )

        private fun testDeserializationOf(functionName: String) {
            compilation.runEventForActionHandler(functionName, properties)
            compilation.assertResults(
                properties["stringList"],
                properties["intList"],
                properties["longList"],
                properties["floatList"],
                properties["doubleList"],
                properties["booleanList"],
                properties["anyList"],
                listOf(rg(), cpf()),
                listOf(document()),
                listOf(myDate()),
                properties["mapList"],
                properties["stringListListList"],
                properties["stringListListList2"],
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
    @DisplayName("When some properties have null list items")
    inner class SomeNullItems {
        private val properties = mapOf(
            "stringList" to listOf(null, null, "test"),
            "intList" to listOf(10, null),
            "longList" to listOf(null, 42L),
            "floatList" to listOf(null, 14.365F),
            "doubleList" to listOf(null, 13.75),
            "booleanList" to listOf(null, true),
            "anyList" to listOf(5, null),
            "enumList" to listOf(null, "RG"),
            "classList" to listOf(
                null,
                mapOf("type" to "CPF", "value" to cpfValue),
            ),
            "customClassList" to listOf(
                null,
                myDateValue,
            ),
            "mapList" to listOf(mapOf("value" to 10), null),
            "stringListListList" to listOf(null, listOf(listOf("test"))),
            "stringListListList2" to listOf(listOf(listOf(null, "test"))),
        )

        private fun testFailureOf(functionName: String) {
            val errors = listOf(
                "Expected a string for property \"stringList[0]\", but found null",
                "Expected a string for property \"stringList[1]\", but found null",
                "Expected a number for property \"intList[1]\", but found null",
                "Expected a number for property \"longList[0]\", but found null",
                "Expected a number for property \"floatList[0]\", but found null",
                "Expected a number for property \"doubleList[0]\", but found null",
                "Expected a boolean for property \"booleanList[0]\", but found null",
                "Expected CPF, RG, CNH for property \"enumList[0]\", but found null",
                "Expected CPF, RG, CNH for property \"classList[0].type\", but found null",
                "Expected a string for property \"classList[0].value\", but found null",
                "Expected an object for property \"mapList[1]\", but found null",
                "Expected an array for property \"stringListListList[0]\", but found null",
                "Expected a string for property \"stringListListList2[0][0][0]\", but found null"
            )
            compilation.runEventForActionHandlerCatching(functionName, properties, errors)
        }

        @DisplayName("Then it should fail to deserialize action with required properties")
        @Test
        fun `should fail to deserialize required action`() = testFailureOf("required")

        @DisplayName("Then it should deserialize action where the list items are optional")
        @Test
        fun `should deserialize optional items action`() {
            compilation.runEventForActionHandler("nullableItems", properties)
            compilation.assertResults(
                properties["stringList"],
                properties["intList"],
                properties["longList"],
                properties["floatList"],
                properties["doubleList"],
                properties["booleanList"],
                properties["anyList"],
                listOf(null, rg()),
                listOf(null, document()),
                // [0] is not null because the custom deserializer is supposed to manage this.
                listOf(myDate(0L), myDate()),
                properties["mapList"],
                properties["stringListListList"],
                properties["stringListListList2"],
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
            "stringList" to listOf("test"),
            "floatList" to listOf(14.365F),
        )

        private fun testFailureOf(functionName: String) {
            val errors = listOf(
                "Expected an array for property \"intList\", but found null",
                "Expected an array for property \"longList\", but found null",
                "Expected an array for property \"doubleList\", but found null",
                "Expected an array for property \"booleanList\", but found null",
                "Expected an array for property \"anyList\", but found null",
                "Expected an array for property \"enumList\", but found null",
                "Expected an array for property \"classList\", but found null",
                "Expected an array for property \"customClassList\", but found null",
                "Expected an array for property \"mapList\", but found null",
                "Expected an array for property \"stringListListList\", but found null",
                "Expected an array for property \"stringListListList2\", but found null",
            )
            compilation.runEventForActionHandlerCatching(functionName, properties, errors)
        }

        @DisplayName("Then it should fail to deserialize action with required properties")
        @Test
        fun `should fail to deserialize required action`() = testFailureOf("required")

        @DisplayName("Then it should fail to deserialize action where the list items are optional")
        @Test
        fun `should fail to deserialize optional items action`() = testFailureOf("nullableItems")

        @DisplayName("Then it should deserialize action with optional properties")
        @Test
        fun `should deserialize optional action`() {
            compilation.runEventForActionHandler("nullable", properties)
            compilation.assertResults(
                properties["stringList"],
                null,
                null,
                properties["floatList"],
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
            "stringList" to "test",
            "intList" to listOf(10, "test"),
            "longList" to 47L,
            "floatList" to listOf(14.365F, true),
            "doubleList" to listOf(false),
            "booleanList" to listOf(10, "test"),
            "anyList" to 10,
            "enumList" to listOf("test"),
            "classList" to listOf(20),
            "customClassList" to listOf(false),
            "mapList" to listOf(mapOf("value" to "test")),
            "stringListListList" to listOf(listOf(10)),
            "stringListListList2" to listOf(true),
        )

        private fun testFailureOf(functionName: String) {
            val errors = listOf(
                "Expected an array for property \"stringList\", but found String",
                "Expected a number for property \"intList[1]\", but found String",
                "Expected an array for property \"longList\", but found Long",
                "Expected a number for property \"floatList[1]\", but found Boolean",
                "Expected a number for property \"doubleList[0]\", but found Boolean",
                "Expected a boolean for property \"booleanList[0]\", but found Int",
                "Expected a boolean for property \"booleanList[1]\", but found String",
                "Expected an array for property \"anyList\", but found Int",
                "Expected CPF, RG, CNH for property \"enumList[0]\", but found test",
                "Expected CPF, RG, CNH for property \"classList[0].type\", but found null",
                "Expected a string for property \"classList[0].value\", but found null",
                "Expected a number for property \"customClassList[0]\", but found Boolean",
                "Expected a number for property \"mapList[0].value\", but found String",
                "Expected an array for property \"stringListListList[0][0]\", but found Int",
                "Expected an array for property \"stringListListList2[0]\", but found Boolean"
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
