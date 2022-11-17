package test.compiler

import androidx.compose.runtime.Composable
import br.zup.com.nimbus.compose.ComponentData
import com.tschuchort.compiletesting.KotlinCompilation
import com.zup.nimbus.core.ActionTriggeredEvent
import test.TestResult
import test.utils.MockAction
import test.utils.MockEvent
import test.utils.MockNode
import test.utils.assertErrors
import java.lang.reflect.InvocationTargetException
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class CompilationResult(private val result: KotlinCompilation.Result) {
    val output = result.messages
    val directory = result.outputDirectory.absolutePath.replace(Regex("/[^/]+$"), "")

    fun loadClass(name: String?): Class<*> = result.classLoader.loadClass(name)
        ?: throw ClassNotFoundException()

    fun loadEnum(name: String, value: String): Enum<*>? {
        val clazz = loadClass(name)
        val enumConstants = clazz.enumConstants as Array<Enum<*>>
        return enumConstants.find { it.name == value }
    }

    private fun loadGeneratedClass(sourceFile: String) = loadClass(
        sourceFile
            .replace("/", ".")
            .replace(Regex("""([^\.]+)\.kt$""")) {
                val name = it.groups[1]?.value ?: "unknown"
                val capitalized = name.replaceFirstChar { name.first().uppercaseChar() }
                "${capitalized}_generatedKt"
            },
    )

    /**
     * Creates an instance of a class with an empty constructor and sets the field "value" with the
     * value passed as parameter.
     */
    fun instanceOf(className: String, value: Any): Any {
        val clazz = loadClass(className)
        val instance = clazz.getDeclaredConstructor().newInstance()
        val setter = clazz.declaredMethods.find { it.name == "setValue" }
            ?: throw(NoSuchMethodError("setValue"))
        setter.invoke(instance, value)
        return instance
    }

    /**
     * Creates an instance of a class using its first constructor which must accept the values
     * passed as parameters.
     */
    fun instanceOf(className: String, values: List<Any?>): Any {
        val clazz = loadClass(className)
        val constructor = clazz.declaredConstructors.firstOrNull()
            ?: throw NoSuchMethodError("constructor")
        return constructor.newInstance(*values.toTypedArray())
    }

    fun runEventForActionHandler(
        functionName: String,
        properties: Map<String, Any?>,
        sourceFile: String = DEFAULT_SOURCE_FILE,
    ) {
        val method = loadGeneratedClass(sourceFile)
            .getDeclaredMethod(functionName, ActionTriggeredEvent::class.java)
        val action = MockAction(properties) { method.invoke(null, it) }
        val event = MockEvent(listOf(action))
        event.run()
    }

    private fun expectToThrow(expectedErrors: List<String>, block: () -> Unit) {
        try {
            block()
            fail("Expected to throw")
        } catch (e: InvocationTargetException) {
            assertTrue(e.targetException is IllegalArgumentException)
            val message = e.targetException?.message ?: ""
            assertErrors(expectedErrors, message)
        }
    }

    fun runEventForActionHandlerCatching(
        functionName: String,
        properties: Map<String, Any?>,
        expectedErrors: List<String>,
        sourceFile: String = DEFAULT_SOURCE_FILE,
    ) {
        expectToThrow(expectedErrors) {
            runEventForActionHandler(functionName, properties, sourceFile)
        }
    }

    fun runOperation(
        functionName: String,
        arguments: List<Any?>,
        sourceFile: String = DEFAULT_SOURCE_FILE,
    ): Any {
        val method = loadGeneratedClass(sourceFile)
            .getDeclaredMethod(functionName, List::class.java)
        return method.invoke(null, arguments)
    }

    fun runOperationCatching(
        functionName: String,
        arguments: List<Any?>,
        expectedErrors: List<String>,
        sourceFile: String = DEFAULT_SOURCE_FILE,
    ) {
        expectToThrow(expectedErrors) {
            runOperation(functionName, arguments, sourceFile)
        }
    }

    fun renderComponent(
        functionName: String,
        properties: Map<String, Any?>? = null,
        sourceFile: String,
        children: @Composable () -> Unit = @Composable {},
    ) {
        val method = loadGeneratedClass(sourceFile)
            .getDeclaredMethod(functionName, ComponentData::class.java)
        val componentData = ComponentData(MockNode(properties), children)
        method.invoke(null, componentData)
    }

    fun renderComponent(
        functionName: String,
        properties: Map<String, Any?>? = null,
        children: @Composable () -> Unit = @Composable {},
    ) = renderComponent(functionName, properties, DEFAULT_SOURCE_FILE, children)

    fun assertOk() = assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

    fun assertProcessorError(vararg expectedMessages: String) {
        assertEquals(KotlinCompilation.ExitCode.INTERNAL_ERROR, result.exitCode)
        expectedMessages.forEach {
            assertContains(result.messages, it)
        }
    }

    fun clearResults() {
        val results = TestResult.fromCompilation(this)
        results.clear()
    }

    fun assertResults(vararg expected: Any?) {
        val results = TestResult.fromCompilation(this).getAll()
        expected.forEachIndexed { index, value ->
            assertEquals(value, results[index], "assertion failed for index $index")
        }
    }

    fun assertEmptyResults() {
        val results = TestResult.fromCompilation(this).getAll()
        assertTrue(results.isEmpty())
    }

    fun hasSomeError() = result.exitCode != KotlinCompilation.ExitCode.OK
    fun hasCompilationError() = result.exitCode == KotlinCompilation.ExitCode.COMPILATION_ERROR
    fun hasProcessorError() = result.exitCode == KotlinCompilation.ExitCode.INTERNAL_ERROR
}