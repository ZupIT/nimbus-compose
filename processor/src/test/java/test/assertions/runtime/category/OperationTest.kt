package test.assertions.runtime.category

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import test.utils.CompilationResult
import test.utils.Snippets
import test.utils.compile
import java.io.File
import java.lang.reflect.InvocationTargetException
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * We won't retest every scenario that has already been tested for actions
 * (test.assertions.runtime.type). Here we test some general cases and particularities of an
 * operation deserialization.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("When an operation is deserialized")
class OperationTest {
    lateinit var compilation: CompilationResult

    @BeforeAll
    fun setup(@TempDir tempDir: File) {
        compilation = compile(
            """
                import java.text.NumberFormat
                import java.util.Currency
                
                ${Snippets.documentAndDocumentType}
                ${Snippets.myDate}
                
                enum class DateUnit { Minute, Hour, Day }
                
                class UnsupportedOperationData(
                    val context: DeserializationContext,
                    val content: @Composable () -> Unit,
                )
                
                @Deserializer
                fun deserializeCurrency(data: AnyServerDrivenData) =
                    Currency.getInstance(data.asString())

                @AutoDeserialize
                fun formatDocument(document: Document) =
                    if (document.type == DocumentType.CPF) {
                        val part1 = document.value.substring(0, 3)
                        val part2 = document.value.substring(3, 6)
                        val part3 = document.value.substring(6, 9)
                        val part4 = document.value.substring(9, 11)
                        "${'$'}{part1}.${'$'}{part2}.${'$'}{part3}-${'$'}{part4}"
                    }
                    else document.value
                    
                @AutoDeserialize
                fun formatCurrency(currency: Currency?, value: Double?): String {
                    val formatter = NumberFormat.getCurrencyInstance()
                    formatter.maximumFractionDigits = 2
                    formatter.currency = currency
                    return formatter.format(value ?: 0)
                }
                
                // Testing vararg
                
                @AutoDeserialize
                fun sum(vararg numbers: Double) = numbers.sum()
                
                @AutoDeserialize
                fun addToDate(date: MyDate, vararg values: Double, unit: DateUnit) = when(unit) {
                    DateUnit.Minute -> date.value + values.sum() * 60000
                    DateUnit.Hour -> date.value + values.sum() * 3600000
                    DateUnit.Day -> date.value + values.sum() * 86400000
                }
                
                @AutoDeserialize
                fun joinStrings(vararg strings: String, separator: String) = strings.joinToString(separator)
                
                @AutoDeserialize
                fun unsupported(data: UnsupportedOperationData) = listOf(
                    data.context.component?.node?.id,
                    data.context.event?.scope?.name,
                    data.content(),
                )
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
    @DisplayName("When we correctly use the operation to format a CPF document, it should format")
    fun `should format cpf`() {
        val arguments = listOf(mapOf("type" to "CPF", "value" to "01244785684"))
        val result = compilation.runOperation("formatDocument", arguments)
        assertEquals("012.447.856-84", result)
    }

    @Test
    @DisplayName("When we call the operation to format a document with an invalid argument, it " +
            "should fail")
    fun `should fail to format invalid document`() {
        val arguments = listOf(mapOf("type" to "SSN"))
        val errors = listOf(
            "Expected CPF, RG, CNH for property \"[0].type\", but found SSN",
            "Expected a string for property \"[0].value\", but found null"
        )
        compilation.runOperationCatching("formatDocument", arguments, errors)
    }

    @Test
    @DisplayName("When we call the operation to format a document with extra arguments, it " +
            "should format ignoring the extra arguments")
    fun `should format document with extra arguments`() {
        val arguments = listOf(mapOf("type" to "CPF", "value" to "01244785684"), "another", 58, true)
        val result = compilation.runOperation("formatDocument", arguments)
        assertEquals("012.447.856-84", result)
    }

    @Test
    @DisplayName("When we call the operation to format a document with no arguments, it should " +
            "fail")
    fun `should fail to format document, insufficient arguments`() {
        val errors = listOf(
            "Expected CPF, RG, CNH for property \"[0].type\", but found null",
            "Expected a string for property \"[0].value\", but found null"
        )
        compilation.runOperationCatching("formatDocument", emptyList(), errors)
    }

    @Test
    @DisplayName("When we correctly use the operation to format a currency value, it should format")
    fun `should format currency`() {
        val arguments = listOf("BRL", 25899.87)
        val result = compilation.runOperation("formatCurrency", arguments)
        // \u00A0 is NBSP, which, visually, is a space character.
        assertEquals("R$\u00A025.899,87", result)
    }

    @Test
    @DisplayName("When we use the operation to format a currency value with invalid arguments, " +
            "it should fail")
    fun `should fail to format currency, invalid arguments`() {
        val arguments = listOf("BRL", true)
        val errors = listOf("Expected a number for property \"[1]\", but found Boolean")
        compilation.runOperationCatching("formatCurrency", arguments, errors)
    }

    @Test
    @DisplayName("When we use the operation to format a currency value and we miss an optional " +
            "parameter, it should format zero")
    fun `should format currency with missing optional parameter`() {
        val arguments = listOf("BRL")
        val result = compilation.runOperation("formatCurrency", arguments)
        // \u00A0 is NBSP, which, visually, is a space character.
        assertEquals("R$\u00A00,00", result)
    }

    @Test
    @DisplayName("When we use the operation to sum numbers, it should work despite the number of " +
            "arguments")
    fun `should sum numbers`() {
        fun sum(numbers: List<Any>) =
            compilation.runOperation("sum", numbers)
        assertEquals(10.0, sum(listOf(10)))
        assertEquals(146.82, sum(listOf(5, 2, 8.98, 20, 32, 78.84)))
        assertEquals(60.0, sum(listOf(10, 20, 30)))
    }

    @Test
    @DisplayName("When we use the operation to sum numbers, it should fail if an argument is " +
            "invalid")
    fun `should fail to sum invalid numbers`() {
        val arguments = listOf(10, 15, true, "20", "test", "30.15", null)
        /* the following expectations are a problem. The ideal error messages would be "(...) for
        property [index], (...)", but given how the support for varargs was implemented, a vararg
        parameter transforms part of the arguments into a sublist, adding another list level
        therefore creating these bad error messages. I'm not sure yet how to fix this issue without
        over complicating the implementation. */
        val errors = listOf(
            "Expected a number for property \"[0][2]\", but found Boolean",
            "Expected a number for property \"[0][4]\", but found String",
            "Expected a number for property \"[0][6]\", but found null",
        )
        compilation.runOperationCatching("sum", arguments, errors)
    }

    @Test
    @DisplayName("When we use the operation to sum numbers and no arguments are passed, it " +
            "should sum zero")
    fun `should sum zero`() {
        assertEquals(0.0, compilation.runOperation("sum", emptyList()))
    }

    @Test
    @DisplayName("When we use the operation to add something to a date, it should work despite " +
            "the number of values")
    fun `should add to date`() {
        fun addToDate(values: List<Any>, unit: String) =
            compilation.runOperation("addToDate", listOf(10L) + values + unit)
        assertEquals(360010.0, addToDate(listOf(1, 2, 3), "Minute"))
        assertEquals(72000010.0, addToDate(listOf(20), "Hour"))
        assertEquals(10.0, addToDate(listOf(), "Hour"))
        assertEquals(777600010.0, addToDate(listOf(3, 6), "Day"))
    }

    @Test
    @DisplayName("When we use the operation to add something to a date with insufficient " +
            "arguments, it should fail")
    fun `should fail to add to date`() {
        try {
            compilation.runOperation("addToDate", emptyList())
            fail("expected to throw")
        } catch (e: InvocationTargetException) {
            assertContains(e.targetException.message ?: "", "insufficient number of arguments")
        }
    }

    @Test
    @DisplayName("When we use the operation to join strings, it should work despite the number " +
            "of strings to join")
    fun `should join strings`() {
        fun joinStrings(values: List<Any>) =
            compilation.runOperation("joinStrings", values + ", ")
        assertEquals(
            "35, hello world, 25.78, true, false, test",
            joinStrings(listOf(35, "hello world", 25.78, true, false, "test")),
        )
        assertEquals("", joinStrings(emptyList()))
        assertEquals("abc", joinStrings(listOf("abc")))
    }

    @Test
    @DisplayName("When we use the operation to join strings with insufficient arguments, " +
            "it should fail")
    fun `should fail to join strings`() {
        try {
            compilation.runOperation("joinStrings", emptyList())
            fail("expected to throw")
        } catch (e: InvocationTargetException) {
            assertContains(e.targetException.message ?: "", "insufficient number of arguments")
        }
    }

    @Test
    @DisplayName("When an operation indirectly requests the deserialization context or the " +
            "component's children, it should receive nulls")
    fun `should inject empty context and empty component to operation`() {
        assertEquals(
            listOf(null, null, Unit),
            compilation.runOperation("unsupported", emptyList()),
        )
    }
}