package test.assertions.runtime.category

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
import kotlin.test.assertEquals

/**
 * Most scenarios for action deserialization have already been tested by
 * "test.assertions.runtime.type". Here we test just some particularities of an action
 * deserialization that have not been tested yet.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("When an action is deserialized")
class ActionTest {
    lateinit var compilation: CompilationResult

    @BeforeAll
    fun setup(@TempDir tempDir: File) {
        compilation = compile(
            """
                class ActionData(
                    val context: DeserializationContext,
                    val content: @Composable () -> Unit,
                )
                
                @AutoDeserialize
                fun myAction(data: ActionData) {
                    TestResult.add(
                        data.context.component?.node?.id,
                        data.context.event?.scope?.name,
                        data.content(),
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
    @DisplayName("When an action indirectly requests the deserialization context or the " +
            "component's children, it should receive null for the component data and the event " +
            "for event data")
    fun `should inject empty context and empty component to operation`() {
        compilation.runEventForActionHandler("myAction", emptyMap())
        compilation.assertResults(null, DEFAULT_EVENT_NAME, Unit)
    }
}