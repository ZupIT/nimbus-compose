package com.zup.nimbus.processor.codegen

import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.FileSpec
import com.zup.nimbus.processor.ClassNames
import com.zup.nimbus.processor.error.NoSourceFile
import com.zup.nimbus.processor.model.DeserializableFunction
import com.zup.nimbus.processor.model.FileToWrite
import com.zup.nimbus.processor.model.FunctionCategory
import com.zup.nimbus.processor.model.FunctionWriterResult
import com.zup.nimbus.processor.utils.getPackageName
import com.zup.nimbus.processor.utils.getSimpleName

class FileWriter(
    private val functionsToDeserialize: List<DeserializableFunction>,
    private val deserializers: List<KSFunctionDeclaration>,
) {
    private var allTypesToDeserialize = mutableSetOf<KSType>()
    private val files = mutableMapOf<KSFile, FileSpec.Builder>()

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

    private fun getFileBuilder(declaration: KSDeclaration): FileSpec.Builder {
        val declaredAt = declaration.containingFile ?: throw NoSourceFile(declaration)
        return files[declaredAt] ?: run {
            val newFile = FileSpec.builder(
                declaration.packageName.asString(),
                declaration.simpleName.asString(),
            )
            files[declaredAt] = newFile
            newFile
        }
    }

    private fun writeDeserializableFunctions() {
        functionsToDeserialize.forEach { fn ->
            val file = getFileBuilder(fn.declaration)
            val result = writeDeserializableFunction(fn)
            result.typesToImport.forEach { file.addImport(it, "") }
            result.functionBuilders.forEach { file.addFunction(it.build()) }
            allTypesToDeserialize.addAll(result.typesToAutoDeserialize)
        }
    }

    private fun writeEntities() {
        var index = 0
        while (index < allTypesToDeserialize.size) {
            val type = allTypesToDeserialize.elementAt(index)
            val file = getFileBuilder(type.declaration)
            val result = EntityWriter(type, deserializers).write()
            result.typesToImport.forEach { file.addImport(it, "") }
            result.functionBuilders.forEach { file.addFunction(it.build()) }
            allTypesToDeserialize.addAll(result.typesToAutoDeserialize)
            index++
        }
    }

    fun write(): List<FileToWrite> {
        writeDeserializableFunctions()
        writeEntities()
        return files.map { FileToWrite(spec = it.value.build(), source = it.key) }
    }
}