package com.zup.nimbus.processor.codegen

import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.FileSpec
import com.zup.nimbus.processor.ClassNames
import com.zup.nimbus.processor.model.DeserializableFunction
import com.zup.nimbus.processor.model.FileToWrite
import com.zup.nimbus.processor.model.FunctionCategory
import com.zup.nimbus.processor.model.FunctionWriterResult

class FileWriter(
    private val functionsToDeserialize: List<DeserializableFunction>,
    private val deserializers: List<KSFunctionDeclaration>,
) {
    private var allTypesToDeserialize = mutableSetOf<KSType>()

    private fun groupByFile(
        functions: List<DeserializableFunction>,
    ): Map<KSFile, List<DeserializableFunction>> {
        val groupedByFile = mutableMapOf<KSFile, MutableList<DeserializableFunction>>()
        functions.forEach { fn ->
            fn.declaration.containingFile?.let { file ->
                val group = groupedByFile[file] ?: run {
                    val newGroup = mutableListOf<DeserializableFunction>()
                    groupedByFile[file] = newGroup
                    newGroup
                }
                group.add(fn)
            }
        }
        return groupedByFile
    }

    private fun writeDeserializableFunction(fn: DeserializableFunction): FunctionWriterResult {
        return when (fn.category) {
            FunctionCategory.Component ->
                ComponentWriter(fn.declaration, deserializers).write()
            FunctionCategory.Action -> {
                // todo
                FunctionWriterResult(emptySet(), emptySet(), emptyList())
            }
            FunctionCategory.Operation -> {
                // todo
                FunctionWriterResult(emptySet(), emptySet(), emptyList())
            }
        }
    }

    private fun writeDeserializableFunctions(): List<FileToWrite> {
        val byFile = groupByFile(functionsToDeserialize)
        return byFile.map { entry ->
            val file = FileSpec.builder(entry.key.packageName.getShortName(), entry.key.fileName)
            val result = FunctionWriterResult.combine(
                entry.value.map { writeDeserializableFunction(it) }
            )
            result.typesToImport.forEach { file.addImport(it) }
            result.functionBuilders.forEach { file.addFunction(it.build()) }
            allTypesToDeserialize.addAll(result.typesToAutoDeserialize)
            FileToWrite(spec = file.build(), source = entry.key)
        }
    }

    private fun writeEntities(): FileToWrite {
        val file = FileSpec.builder(
            ClassNames.EntityDeserializer.packageName,
            ClassNames.EntityDeserializer.simpleName,
        )
        var index = 0
        while (index < allTypesToDeserialize.size) {
            val type = allTypesToDeserialize.elementAt(index)
            val result = EntityWriter(type, deserializers).write()
            result.typesToImport.forEach { file.addImport(it) }
            result.functionBuilders.forEach { file.addFunction(it.build()) }
            allTypesToDeserialize.addAll(result.typesToAutoDeserialize)
            index++
        }
        // fixme: the source is wrong
        return FileToWrite(spec = file.build(), source = allTypesToDeserialize.first().declaration.containingFile!!)
    }

    fun write(): List<FileToWrite> {
        return writeDeserializableFunctions() + writeEntities()
    }
}