package test.assertions.runtime.category

import androidx.compose.material.printedByTextComponent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import br.zup.com.nimbus.compose.MockLogger
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import test.BaseRuntimeTest
import test.utils.DEFAULT_COMPONENT_ID
import test.utils.MockAction
import test.utils.MockEvent
import test.utils.Snippets
import test.utils.assertErrors
import test.compiler.TestCompiler
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val ERROR_TEXT = "Error while deserializing component. Check the logs for details."
private val errorColor = Color.Red

/**
 * We won't retest every scenario that has already been tested for actions
 * (test.assertions.runtime.type). Here we test some general cases and particularities of a
 * component deserialization.
 */
@DisplayName("When a component is deserialized")
class ComponentTest: BaseRuntimeTest() {
    private val cpfValue = "01444752174"
    private val myDateValue = 15L

    @BeforeAll
    fun setup() {
        compilation = TestCompiler.compile(
            """
                ${Snippets.myDate}
                ${Snippets.documentAndDocumentType}
                
                class MyComponentData(
                  val context: DeserializationContext,
                  val content: @Composable () -> Unit,
                )
                
                @AutoDeserialize
                @Composable
                fun LeafComponent(
                    name: String,
                    date: MyDate,
                    metadata: Any,
                    age: Int?,
                    onPress: () -> Unit,
                    documents: List<Document>,
                ) {
                    onPress()
                    TestResult.add(name, date, metadata, age, documents)
                }
                
                @AutoDeserialize
                @Composable
                fun ContainerComponent(content: @Composable () -> Unit) {
                    content()
                    TestResult.add("rendered")
                }
                
                @AutoDeserialize
                @Composable
                fun ContextAwareComponent(context: DeserializationContext) {
                    TestResult.add(context.component?.node?.id)
                }
                
                @AutoDeserialize
                @Composable
                fun ClassPropsComponent(data: MyComponentData) {
                    data.content()
                    TestResult.add(data.context.component?.node?.id, data.context.event?.scope?.name)
                }
            """,
        )
    }

    private fun myDate(value: Long = myDateValue) = compilation.instanceOf("MyDate", value)
    private fun cpf() = compilation.loadEnum("DocumentType", "CPF")
    private fun document() = compilation.instanceOf("Document", listOf(cpf(), cpfValue))

    @DisplayName("When the properties are correctly given, it should deserialize the LeafComponent")
    @Test
    fun `should deserialize LeafComponent if properties are correct`() {
        var wasOnPressCalled = false
        val onPress = MockEvent(listOf(MockAction(null) {
            wasOnPressCalled = true
        }))
        val properties = mapOf(
            "name" to "test",
            "date" to myDateValue,
            "metadata" to "meta",
            "onPress" to onPress,
            "documents" to listOf(mapOf("type" to "CPF", "value" to cpfValue))
        )
        compilation.renderComponent("LeafComponent", properties)
        assertTrue(wasOnPressCalled)
        compilation.assertResults("test", myDate(), "meta", null, listOf(document()))
        assertTrue(printedByTextComponent.isEmpty())
        assertTrue(MockLogger.errors.isEmpty())
    }

    @DisplayName("When some required properties are missing and others are invalid, it should " +
            "fail to deserialize the LeafComponent")
    @Test
    fun `should fail to deserialize LeafComponent if properties are incorrect`() {
        val properties = mapOf(
            "age" to 23,
            "onPress" to "event",
            "documents" to 20,
            "metadata" to "",
        )
        val errors = listOf(
            "Expected a string for property \"name\", but found null",
            "Expected an event for property \"onPress\", but found String",
            "Expected a list for property \"documents\", but found Int",
        )
        compilation.renderComponent("LeafComponent", properties)
        compilation.assertEmptyResults()
        assertEquals(
            listOf(ERROR_TEXT to errorColor),
            printedByTextComponent,
        )
        assertEquals(1, MockLogger.errors.size)
        assertErrors(errors, MockLogger.errors.first())
    }

    @DisplayName("When the children is correctly given to the ContainerComponent, it should " +
            "deserialize")
    @Test
    fun `should deserialize ContainerComponent`() {
        var isContentRendered = false
        compilation.renderComponent("ContainerComponent", null) @Composable {
            isContentRendered = true
        }
        assertTrue(isContentRendered)
        compilation.assertResults("rendered")
        assertTrue(printedByTextComponent.isEmpty())
        assertTrue(MockLogger.errors.isEmpty())
    }

    @DisplayName("When the children is not provided to the ContainerComponent, it should render " +
            "no child")
    @Test
    fun `should render no child if no children is provided`() {
        compilation.renderComponent("ContainerComponent")
        compilation.assertResults("rendered")
        assertTrue(printedByTextComponent.isEmpty())
        assertTrue(MockLogger.errors.isEmpty())
    }

    @DisplayName("When the deserialization context is requested by the ContextAwareComponent, " +
            "it should be injected.")
    @Test
    fun `should inject context into ContextAwareComponent`() {
        compilation.renderComponent("ContextAwareComponent")
        compilation.assertResults(DEFAULT_COMPONENT_ID)
        assertTrue(printedByTextComponent.isEmpty())
        assertTrue(MockLogger.errors.isEmpty())
    }

    @DisplayName("When the deserialization context and the children are requested by the class " +
            "MyComponentData (via ClassPropsComponent), both should be injected.")
    @Test
    fun `should inject context and children into MyComponentData via ClassPropsComponent`() {
        var isContentRendered = false
        compilation.renderComponent("ClassPropsComponent", null) @Composable {
            isContentRendered = true
        }
        assertTrue(isContentRendered)
        compilation.assertResults(DEFAULT_COMPONENT_ID, null)
        assertTrue(printedByTextComponent.isEmpty())
        assertTrue(MockLogger.errors.isEmpty())
    }
}