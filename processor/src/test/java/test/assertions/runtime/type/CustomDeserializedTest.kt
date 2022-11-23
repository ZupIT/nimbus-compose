package test.assertions.runtime.type

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import test.BaseRuntimeTest
import test.utils.DEFAULT_ACTION_NAME
import test.utils.Snippets
import test.compiler.TestCompiler

@DisplayName("When action handlers with custom deserialized types are deserialized")
class CustomDeserializedTest: BaseRuntimeTest() {
    private val myDateValue = 30L
    private val custom1Value = "custom1"
    private val custom2Value = "custom2"

    @BeforeAll
    fun setup() {
        compilation = TestCompiler.compile(
            """
                ${Snippets.myDate}
                
                class Custom1 {
                  var value: String = ""
                  override fun equals(other: Any?) = other is Custom1 && value == other.value
                  override fun toString() = value
                }
                
                class Custom2 {
                  var value: String = ""
                  override fun equals(other: Any?) = other is Custom2 && value == other.value
                  override fun toString() = value
                }
                
                class Custom3<T> {
                  var value: T? = null
                  override fun equals(other: Any?) = other is Custom3<*> && value == other.value
                  override fun toString() = "${'$'}value"
                }
                
                @Deserializer
                fun deserializeCustom1(
                  data: AnyServerDrivenData,
                  context: DeserializationContext,
                ): Custom1? {
                    val value = data.asStringOrNull()
                    return value?.let {
                        val result = Custom1()
                        result.value = "${'$'}it ${'$'}{context.event?.action?.name}"
                        result
                    } ?: null
                }
                
                @Deserializer
                fun deserializeCustom2(
                  context: DeserializationContext,
                  data: AnyServerDrivenData,
                ): Custom2 {
                    val value = data.asStringOrNull() ?: ""
                    val result = Custom2()
                    result.value = "${'$'}value ${'$'}{context.event?.action?.name}"
                    return result
                }
                
                @Deserializer
                fun deserializeCustom3(context: DeserializationContext): Custom3<String> {
                    val result = Custom3<String>()
                    result.value = context.event?.action?.name ?: ""
                    return result
                }
                
                @AutoDeserialize
                fun customTypeAction(
                  myDate: MyDate,
                  custom1: Custom1?,
                  custom2: Custom2,
                  custom3: Custom3<String>?,
                ) {
                  TestResult.add(myDate, custom1, custom2, custom3)
                }
            """,
        )
    }

    private fun myDate(value: Long = myDateValue) = compilation.instanceOf("MyDate", value)
    private fun custom1(value: String = custom1Value) = compilation.instanceOf(
    "Custom1",
    "$value $DEFAULT_ACTION_NAME",
    )
    private fun custom2(value: String = custom2Value) = compilation.instanceOf(
    "Custom2",
    "$value $DEFAULT_ACTION_NAME",
    )
    private fun custom3() = compilation.instanceOf("Custom3", DEFAULT_ACTION_NAME)

    @DisplayName("When all properties are given correctly, it should deserialize")
    @Test
    fun `should deserialize all properties if they're given correctly`() {
        val properties = mapOf(
            "myDate" to myDateValue,
            "custom1" to custom1Value,
            "custom2" to custom2Value,
        )
        compilation.runEventForActionHandler("customTypeAction", properties)
        compilation.assertResults(myDate(), custom1(), custom2(), custom3())
    }

    @DisplayName("When optional properties are missing, it should deserialize")
    @Test
    fun `should deserialize if optional properties are missing`() {
        val properties = mapOf(
            "myDate" to myDateValue,
            "custom2" to custom2Value,
        )
        compilation.runEventForActionHandler("customTypeAction", properties)
        compilation.assertResults(myDate(), null, custom2(), custom3())
    }

    @DisplayName("When required properties are missing, it should deserialize (custom " +
            "deserializer for Custom2 is treating this scenario)")
    @Test
    fun `should deserialize even if required properties are missing`() {
        val properties = mapOf(
            "custom1" to custom1Value,
        )
        compilation.runEventForActionHandler("customTypeAction", properties)
        compilation.assertResults(myDate(0L), custom1(), custom2(""), custom3())
    }

    @DisplayName("When a property is invalid, it should fail")
    @Test
    fun `should not deserialize if property is invalid`() {
        val properties = mapOf(
            "myDate" to "invalid",
            "custom1" to custom1Value,
            "custom2" to custom2Value,
        )
        val errors = listOf("Expected a number for property \"myDate\", but found String")
        compilation.runEventForActionHandlerCatching("customTypeAction", properties, errors)
    }
}
