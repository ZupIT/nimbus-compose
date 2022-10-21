package com.zup.nimbus.processor

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.validate
import com.zup.nimbus.processor.codegen.FileWriter
import com.zup.nimbus.processor.model.FileToWrite

class Processor(private val environment: SymbolProcessorEnvironment): SymbolProcessor {
    companion object {
        private val extensionRegex = Regex("""\.[^.]+${'$'}""")
    }

    private fun writeFile(file: FileToWrite, dependencies:  Array<KSFile>) {
        environment.codeGenerator.createNewFile(
            @Suppress("SpreadOperator")
            Dependencies(false, *(dependencies + file.source)),
            file.source.packageName.asString(),
            file.source.fileName.replace(extensionRegex, ".generated"),
        ).write(file.spec.toString().toByteArray())
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val functionsToDeserialize = AnnotationCollector.collectDeserializableFunctions(resolver)
        val customDeserializers = AnnotationCollector.collectCustomDeserializers(resolver)
        /* we must use all custom deserializers as dependencies of every file because they must
        always be evaluated when generating the code of an auto-deserializer. */
        val dependencies = customDeserializers.mapNotNull { it.containingFile }.toTypedArray()
        val filesToWrite = FileWriter.write(functionsToDeserialize, customDeserializers, resolver)
        filesToWrite.forEach { writeFile(it, dependencies) }
        return (functionsToDeserialize.map { it.declaration } + customDeserializers)
            .filterNot { it.validate() }
            .toList()
    }
}
