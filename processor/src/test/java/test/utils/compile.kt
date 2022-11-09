package test.utils

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspWithCompilation
import com.tschuchort.compiletesting.symbolProcessorProviders
import com.zup.nimbus.processor.Main
import java.io.File

const val DEFAULT_SOURCE_FILE = "MyTest.kt"
private const val SRC_DIR = "sources"

private fun getCompilationResult(sources: List<SourceFile>, tempDir: File) = CompilationResult(
    KotlinCompilation().apply {
        workingDir = tempDir
        this.sources = sources
        symbolProcessorProviders = listOf(Main())
        inheritClassPath = true
        messageOutputStream = System.out // see diagnostics in real time
        kspWithCompilation = true
    }.compile()
)

fun compile(code: String, tempDir: File): CompilationResult {
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
    return getCompilationResult(listOf(src), tempDir)
}

fun compile(sourceMap: Map<String, String>, tempDir: File): CompilationResult {
    val sources = sourceMap.map {
        val dir = it.key.replace(Regex("""[^/]+\.kt$"""), "")
        File("${tempDir.absolutePath}/${SRC_DIR}/$dir").mkdirs()
        SourceFile.kotlin(it.key, it.value)
    }
    return getCompilationResult(sources, tempDir)
}
