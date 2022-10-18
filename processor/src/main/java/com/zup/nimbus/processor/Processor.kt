package com.zup.nimbus.processor

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.validate
import com.zup.nimbus.processor.codegen.FileWriter
import com.zup.nimbus.processor.model.FileToWrite

class Processor(private val environment: SymbolProcessorEnvironment): SymbolProcessor {
    companion object {
        private val extensionRegex = Regex("""\.[^.]+${'$'}""")
    }

    private fun writeFile(file: FileToWrite) {
        environment.codeGenerator.createNewFile(
            Dependencies(false, file.source),
            file.source.packageName.asString(),
            file.source.fileName.replace(extensionRegex, ".generated"),
        ).write(file.spec.toString().toByteArray())
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val functionsToDeserialize = AnnotationCollector.collectDeserializableFunctions(resolver)
        val customDeserializers = AnnotationCollector.collectCustomDeserializers(resolver)
        val filesToWrite = FileWriter.write(functionsToDeserialize, customDeserializers)
        filesToWrite.forEach { writeFile(it) }
        return (functionsToDeserialize.map { it.declaration } + customDeserializers)
            .filterNot { it.validate() }
            .toList()
    }
}
