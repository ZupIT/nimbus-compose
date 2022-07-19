package com.zup.nimbus.annotation

//import androidx.compose.runtime.Composable
//import br.zup.com.nimbus.compose.ComponentData
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.writeTo
import kotlin.reflect.KClass

class ServerDrivenProcessor(private val environment: SymbolProcessorEnvironment): SymbolProcessor {
    private fun Resolver.findAnnotations(
        kClass: KClass<*>,
    ) = getSymbolsWithAnnotation(
        kClass.qualifiedName.toString())
        .filterIsInstance<KSFunctionDeclaration>()

    private fun createNimbusComposable(builder: FileSpec.Builder, fn: KSFunctionDeclaration) {
        builder.addFunction(
            FunSpec.builder(fn.simpleName.asString())
                //.addAnnotation(Composable::class)
                //.addParameter("data", ComponentData::class)
                .addStatement("println(%P)", "Hello World")
                .build()
        )
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val functions: Sequence<KSFunctionDeclaration> =
            resolver.findAnnotations(ServerDrivenComponent::class)
        if(!functions.iterator().hasNext()) return emptyList()

        val sourceFiles = functions.mapNotNull { it.containingFile }

        /*val poetFile = FileSpec.builder(
            "br.com.zup.nimbus.compose.sample.components",
            "generatedComponents",
        )

        functions.forEach { createNimbusComposable(poetFile, it) }

        poetFile.build().writeTo(
            codeGenerator = environment.codeGenerator,
            aggregating = false,
            originatingKSFiles = sourceFiles.toList(),
        )*/

        val file = environment.codeGenerator.createNewFile(
            Dependencies(
                false,
                *sourceFiles.toList().toTypedArray(),
            ),
            "br.com.zup.nimbus.compose.sample.components",
            "generatedComponents"
        )

        file.write("// MY TEST".toByteArray())

        return (functions).filterNot { it.validate() }.toList()
    }
}