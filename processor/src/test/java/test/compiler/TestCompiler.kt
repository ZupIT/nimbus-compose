package test.compiler

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspWithCompilation
import com.tschuchort.compiletesting.symbolProcessorProviders
import br.com.zup.nimbus.processor.Main
import java.io.File
import java.io.OutputStream
import java.util.UUID

object TestCompiler {
    var lastCompilationResult: CompilationResult? = null

    fun clearCompilationDirectory() {
        File("${System.getProperty("user.dir")}/$TEST_DIR").deleteRecursively()
    }

    private fun newCompilationDirectory() = File(
        "${System.getProperty("user.dir")}/$TEST_DIR/${UUID.randomUUID()}",
    )

    private fun processCompilationResult(
        sources: List<SourceFile>,
        workingDir: File,
    ): CompilationResult {
        lastCompilationResult = CompilationResult(KotlinCompilation().apply {
            this.workingDir = workingDir
            this.sources = sources
            symbolProcessorProviders = listOf(Main())
            inheritClassPath = true
            // prevents any logging to the console
            messageOutputStream = OutputStream.nullOutputStream()
            kspWithCompilation = true
        }.compile())
        return lastCompilationResult!!
    }

    fun compile(code: String): CompilationResult {
        val src = SourceFile.kotlin(
            DEFAULT_SOURCE_FILE,
            """
            import test.TestResult
            import br.com.zup.nimbus.annotation.AutoDeserialize
            import br.com.zup.nimbus.annotation.Deserializer
            import br.com.zup.nimbus.core.deserialization.AnyServerDrivenData
            import br.com.zup.nimbus.processor.deserialization.DeserializationContext
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
