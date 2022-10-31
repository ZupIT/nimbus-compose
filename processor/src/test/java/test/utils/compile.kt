package test.utils

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.symbolProcessorProviders
import com.zup.nimbus.core.ActionTriggeredEvent
import com.zup.nimbus.core.tree.ServerDrivenEvent
import com.zup.nimbus.processor.Main
import test.TestResult
import java.io.File
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val DEFAULT_SOURCE_FILE = "MyTest.kt"
private const val SRC_DIR = "sources"
private const val KSP_SRC_DIR = "ksp/sources/kotlin"

class CompilationResult(private val result: KotlinCompilation.Result) {
    fun loadClass(name: String?): Class<*> = result.classLoader.loadClass(name)
        ?: throw ClassNotFoundException()

    fun loadSourceClass() = loadClass(
        DEFAULT_SOURCE_FILE.replace(".kt", "Kt"),
    )

    fun loadGeneratedClass() = loadClass(
        DEFAULT_SOURCE_FILE.replace(".kt", "_generatedKt"),
    )

    fun runEventForActionHandler(
        functionName: String,
        properties: Map<String, Any?>,
    ) {
        val method = loadGeneratedClass()
            .getDeclaredMethod(functionName, ActionTriggeredEvent::class.java)
        val action = MockAction(properties) { method.invoke(null, it) }
        val event = MockEvent(listOf(action))
        event.run()
    }

    fun runEventForActionHandlerCatching(
        functionName: String,
        properties: Map<String, Any?>,
        expectedErrors: List<String>,
    ) {
        try {
            runEventForActionHandler(functionName, properties)
        } catch (e: InvocationTargetException) {
            assertTrue(e.targetException is IllegalArgumentException)
            val message = e.targetException?.message ?: ""
            expectedErrors.forEach { assertContains(message, it) }
        }
    }

    fun assertOk() = assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

    fun clearResults() {
        val results = TestResult.fromCompilation(this)
        results.clear()
    }

    fun assertResults(vararg expected: Any?) {
        val results = TestResult.fromCompilation(this).getAll()
        expected.forEachIndexed { index, value ->
            assertEquals(value, results[index])
        }
    }

    fun assertEmptyResults() {
        val results = TestResult.fromCompilation(this).getAll()
        assertTrue(results.isEmpty())
    }
}

private fun processCompilationResult(
    sources: List<SourceFile>,
    tempDir: File,
    includeProcessor: Boolean = true,
) = KotlinCompilation().apply {
    workingDir = tempDir
    this.sources = sources
    symbolProcessorProviders = if (includeProcessor) listOf(Main()) else emptyList()
    inheritClassPath = true
    messageOutputStream = System.out // see diagnostics in real time
}.compile()

private fun compileWithProcessor(code: String, tempDir: File) {
    val src = SourceFile.kotlin(
        DEFAULT_SOURCE_FILE,
        """
            import test.TestResult
            import br.com.zup.nimbus.annotation.AutoDeserialize

            $code
            """
    )
    processCompilationResult(listOf(src), tempDir)
}

private fun recompileWithoutProcessor(tempDir: File): KotlinCompilation.Result {
    val source = File("${tempDir.absolutePath}/$SRC_DIR/$DEFAULT_SOURCE_FILE")
    val generatedFileName = DEFAULT_SOURCE_FILE.replace(".kt", ".generated.kt")
    val generated = File("${tempDir.absolutePath}/${KSP_SRC_DIR}/$generatedFileName")
    val sourceFiles = listOf(
        SourceFile.fromPath(source),
        SourceFile.fromPath(generated),
    )
    return processCompilationResult(sourceFiles, tempDir, false)
}

fun compile(code: String, tempDir: File): CompilationResult {
    /* We must compile the code twice, including the generated source in the second compilation.
    This is because kotlin-compile-testing still doesn't fully support KSP. Although the files are
    generated, they don't get compiled.
    https://github.com/tschuchortdev/kotlin-compile-testing/issues/312. */
    compileWithProcessor(code, tempDir)
    val result = recompileWithoutProcessor(tempDir)
    return CompilationResult(result)
}
