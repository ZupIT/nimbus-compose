package test.assertions.runtime.type

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import test.utils.CompilationResult
import test.utils.MockAction
import test.utils.MockEvent
import test.utils.compile
import java.io.File
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("When action handlers with event functions are deserialized")
class EventTest {
    lateinit var compilation: CompilationResult
    var calls = mutableMapOf<String, Any?>()

    @BeforeAll
    fun setup(@TempDir tempDir: File) {
        compilation = compile(
            """
                class MoreEvents(
                  val onSuccess: (data: Map<String, Any>) -> Unit,
                  val onError: ((String) -> Unit)?,
                  val onFinish: (() -> Unit)?,
                )
                
                @AutoDeserialize
                fun nestedEventsAction(
                  onStart: () -> Unit,
                  onCancel: ((time: Long) -> Unit)?,
                  more: MoreEvents,
                ) {
                    onStart()
                    onCancel?.invoke(10L)
                    more.onSuccess(mapOf("date" to "test"))
                    more.onError?.invoke("test")
                    more.onFinish?.invoke()
                }
                
                @AutoDeserialize
                fun optionalStringAction(
                  value: String?,
                  onChange: (String?) -> Unit,
                ) {
                    onChange(value)
                }
            """,
            tempDir,
        )
        compilation.assertOk()
    }

    @BeforeEach
    fun clear() {
        calls = mutableMapOf()
    }

    private fun createEvent(eventName: String) = MockEvent(listOf(MockAction(null) {
        calls[eventName] = (it.scope as MockEvent).currentStateValue
    }))

    @DisplayName("When all properties are given correctly, it should deserialize")
    @Test
    fun `should deserialize when all properties are given correctly`() {
        val properties = mapOf(
            "onStart" to createEvent("onStart"),
            "onCancel" to createEvent("onCancel"),
            "more" to mapOf(
                "onSuccess" to createEvent("onSuccess"),
                "onError" to createEvent("onError"),
                "onFinish" to createEvent("onFinish"),
            )
        )
        compilation.runEventForActionHandler("nestedEventsAction", properties)
        assertEquals(mapOf(
            "onStart" to null,
            "onCancel" to 10L,
            "onSuccess" to mapOf("date" to "test"),
            "onError" to "test",
            "onFinish" to null,
        ), calls)
    }

    @DisplayName("When the optional properties are missing, it should deserialize")
    @Test
    fun `should deserialize when optional properties are missing`() {
        val properties = mapOf(
            "onStart" to createEvent("onStart"),
            "more" to mapOf(
                "onSuccess" to createEvent("onSuccess"),
            )
        )
        compilation.runEventForActionHandler("nestedEventsAction", properties)
        assertEquals(mapOf<String, Any?>(
            "onStart" to null,
            "onSuccess" to mapOf("date" to "test"),
        ), calls)
    }

    @DisplayName("When some required properties are missing, it should fail")
    @Test
    fun `should fail when only required property is missing`() {
        val properties = mapOf(
            "onCancel" to createEvent("onCancel"),
        )
        val errors = listOf(
            "Expected an event, i.e. an array of actions. for property \"onStart\", but found null",
            "Expected an event, i.e. an array of actions. for property \"more.onSuccess\", but found null",
        )
        compilation.runEventForActionHandlerCatching("nestedEventsAction", properties, errors)
    }

    @DisplayName("When some properties are invalid, it should fail.")
    @Test
    fun `should fail when some properties are invalid`() {
        val properties = mapOf(
            "onStart" to createEvent("onStart"),
            "onCancel" to "test",
            "more" to mapOf(
                "onSuccess" to 10,
                "onError" to true,
                "onFinish" to createEvent("onFinish"),
            )
        )
        val errors = listOf(
            "Expected an event, i.e. an array of actions. for property \"onCancel\", but found String",
            "Expected an event, i.e. an array of actions. for property \"more.onSuccess\", but found Int",
            "Expected an event, i.e. an array of actions. for property \"more.onError\", but found Boolean",
        )
        compilation.runEventForActionHandlerCatching("nestedEventsAction", properties, errors)
    }

    @DisplayName("When null is passed to the onChange event, it should run the event with null as " +
            "the value of the implicit state")
    @Test
    fun `should run onChange event with null as the state value`() {
        val properties = mapOf(
            "value" to null,
            "onChange" to createEvent("onChange"),
        )
        compilation.runEventForActionHandler("optionalStringAction", properties)
        assertEquals(mapOf<String, Any?>("onChange" to null), calls)
    }

    @DisplayName("When 'test' is passed to the onChange event, it should run the event with null " +
            "as the value of the implicit state")
    @Test
    fun `should run onChange event with 'test' as the state value`() {
        val properties = mapOf(
            "value" to "test",
            "onChange" to createEvent("onChange"),
        )
        compilation.runEventForActionHandler("optionalStringAction", properties)
        assertEquals(mapOf<String, Any?>("onChange" to "test"), calls)
    }
}
