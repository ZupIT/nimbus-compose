package com.zup.nimbus.processor.codegen

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.FileSpec
import com.zup.nimbus.processor.error.NoSourceFile
import com.zup.nimbus.processor.model.DeserializableFunction
import com.zup.nimbus.processor.model.FileToWrite
import com.zup.nimbus.processor.model.FunctionCategory
import com.zup.nimbus.processor.model.FunctionWriterResult

internal object FileWriter {
    fun write(
        functionsToDeserialize: List<DeserializableFunction>,
        deserializers: List<KSFunctionDeclaration>,
        resolver: Resolver,
    ): List<FileToWrite> {
        val allTypesToDeserialize = mutableSetOf<KSType>()
        val files = mutableMapOf<KSFile, FileSpec.Builder>()

        fun writeDeserializableFunction(fn: DeserializableFunction): FunctionWriterResult {
            return when (fn.category) {
                FunctionCategory.Component ->
                    ComponentWriter.write(fn.declaration, deserializers)
                FunctionCategory.Action ->
                    ActionWriter.write(fn.declaration, deserializers)
                FunctionCategory.Operation ->
                    OperationWriter.write(fn.declaration, deserializers, resolver)
            }
        }

        fun getFileBuilder(declaration: KSDeclaration): FileSpec.Builder {
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

        fun writeDeserializableFunctions() {
            functionsToDeserialize.forEach { fn ->
                val file = getFileBuilder(fn.declaration)
                val result = writeDeserializableFunction(fn)
                result.typesToImport.forEach { file.addImport(it, "") }
                result.functionBuilders.forEach { file.addFunction(it.build()) }
                allTypesToDeserialize.addAll(result.typesToAutoDeserialize)
            }
        }

        fun writeEntities() {
            var index = 0
            while (index < allTypesToDeserialize.size) {
                val type = allTypesToDeserialize.elementAt(index)
                val file = getFileBuilder(type.declaration)
                val result = EntityWriter.write(type, deserializers)
                result.typesToImport.forEach { file.addImport(it, "") }
                result.functionBuilders.forEach { file.addFunction(it.build()) }
                allTypesToDeserialize.addAll(result.typesToAutoDeserialize)
                index++
            }
        }

        fun main(): List<FileToWrite> {
            writeDeserializableFunctions()
            writeEntities()
            return files.map { FileToWrite(spec = it.value.build(), source = it.key) }
        }

        return main()
    }
}