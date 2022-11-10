package test.compiler

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspWithCompilation
import com.tschuchort.compiletesting.symbolProcessorProviders
import com.zup.nimbus.processor.Main
import org.junit.jupiter.api.extension.ExtensionContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import java.lang.reflect.Method
import java.util.Optional
import java.util.UUID

class TestCompiler(private val context: ExtensionContext?) {
    private fun newOutputStream(): OutputStream {
        val output = ByteArrayOutputStream()
        context?.let {
            val key = getStoreKey(
                it.testClass.get(),
                if (it.testMethod.isEmpty) null else it.testMethod.get(),
            )
            it.getStore(ExtensionContext.Namespace.GLOBAL).put(key, output)
        }
        return output
    }

    companion object {
        fun getStoreKey(clazz: Class<*>, method: Method? = null) =
            "${clazz.name}#${method?.name ?: ""}#$COMPILATION_OUTPUT_KEY"

        fun clearCompilationDirectory() {
            File("${System.getProperty("user.dir")}/$TEST_DIR").deleteRecursively()
        }

        private fun newCompilationDirectory() = File(
            "${System.getProperty("user.dir")}/$TEST_DIR/${UUID.randomUUID()}",
        )
    }

    private fun processCompilationResult(sources: List<SourceFile>, workingDir: File) =
        CompilationResult(KotlinCompilation().apply {
            this.workingDir = workingDir
            this.sources = sources
            symbolProcessorProviders = listOf(Main())
            inheritClassPath = true
            // prevents any logging to the console
            messageOutputStream = newOutputStream()
            kspWithCompilation = true
        }.compile())

    fun compile(code: String): CompilationResult {
        val src = SourceFile.kotlin(
            DEFAULT_SOURCE_FILE,
            """
            import test.TestResult
            import br.com.zup.nimbus.annotation.AutoDeserialize
            import br.com.zup.nimbus.annotation.Deserializer
            import com.zup.nimbus.core.deserialization.AnyServerDrivenData
            import br.zup.com.nimbus.compose.deserialization.DeserializationContext
            import androidx.compose.runtime.Composable

            $code
            """
        )
        return processCompilationResult(listOf(src), newCompilationDirectory())
    }

    fun compile(sourceMap: Map<String, String>): CompilationResult {
        val workingDir = newCompilationDirectory()
        workingDir.mkdirs()
        val sources = sourceMap.map {
            val dir = it.key.replace(Regex("""[^/]+\.kt$"""), "")
            File("${workingDir.absolutePath}/${SRC_DIR}/$dir").mkdirs()
            SourceFile.kotlin(it.key, it.value)
        }
        return processCompilationResult(sources, workingDir)
    }
}
