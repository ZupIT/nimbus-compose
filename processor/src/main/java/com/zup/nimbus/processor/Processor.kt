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

/**
 * Process the annotations for Nimbus Compose.
 */
class Processor(private val environment: SymbolProcessorEnvironment): SymbolProcessor {
    companion object {
        private val extensionRegex = Regex("""\.[^.]+${'$'}""")
    }

    /**
     * Gets a file spec, writes it to the file system and set its source file (if any) as a
     * dependency, so, only if it gets altered, the file is reprocessed.
     *
     * The file is also reprocessed if any file with a function annotated with "@Deserializer"
     * is changed.
     */
    private fun writeFile(file: FileToWrite, dependencies:  Array<KSFile>) {
        val allDependencies = if (file.source == null) dependencies else dependencies + file.source
        val packageName = file.source?.packageName?.asString() ?: file.spec.packageName
        val fileName = file.source?.fileName?.replace(extensionRegex, ".generated") ?:
            "${file.spec.name}.generated"
        environment.codeGenerator.createNewFile(
            Dependencies(false, *allDependencies),
            packageName,
            fileName,
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
        // we don't need more cycles of annotation processing. Everything is done at this point.
        return emptyList()
    }
}
