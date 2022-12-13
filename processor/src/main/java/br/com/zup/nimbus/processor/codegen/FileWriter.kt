/*
 * Copyright 2023 ZUP IT SERVICOS EM TECNOLOGIA E INOVACAO SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.zup.nimbus.processor.codegen

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.FileSpec
import br.com.zup.nimbus.processor.model.DeserializableFunction
import br.com.zup.nimbus.processor.model.FileToWrite
import br.com.zup.nimbus.processor.model.FunctionCategory
import br.com.zup.nimbus.processor.model.FunctionWriterResult

internal object FileWriter {
    /**
     * Writes every auto-deserialized component, action handler, operation and class into file
     * builders (encapsulated in FileToWrite). There will be one file builder for each source file.
     */
    fun write(
        functionsToDeserialize: List<DeserializableFunction>,
        deserializers: List<KSFunctionDeclaration>,
        resolver: Resolver,
    ): List<FileToWrite> {
        /**
         * Holds references to all types that must be deserialized in order for the generated code
         * to work properly. After deserializing all components, action handlers and operations,
         * we'll use this set to write the deserializers for the auto-deserialized classes.
         */
        val allTypesToDeserialize = mutableSetOf<KSType>()
        /**
         * This map holds a reference to each file builder corresponding to an existing source
         * file. This is useful because if multiple functions annotated with `@AutoDeserialize`
         * are in the same file, the file builder must be reused.
         */
        val files = mutableMapOf<KSFile, FileSpec.Builder>()
        /**
         * It is possible that an entity doesn't have a corresponding source file. This happens
         * if a class of an external module is used and no custom deserializer is provided for
         * it. In this case, we'll create a new file without any correspondence with a source file.
         */
        val sourceLessFiles = mutableListOf<FileSpec.Builder>()

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

        fun createFile(declaration: KSDeclaration) = FileSpec.builder(
            declaration.packageName.asString(),
            declaration.simpleName.asString(),
        )

        fun getFileBuilder(declaration: KSDeclaration): FileSpec.Builder {
            val declaredAt = declaration.containingFile
            return files[declaredAt] ?: run {
                val newFile = createFile(declaration)
                if (declaredAt == null) sourceLessFiles.add(newFile)
                else files[declaredAt] = newFile
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

        /**
         * Writes the auto-deserialized classes. Notice that this process may yield more classes
         * that need to be auto-deserialized, hence the `while` loop.
         */
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
            return files.map { FileToWrite(spec = it.value.build(), source = it.key) } +
                    sourceLessFiles.map { FileToWrite(spec = it.build(), source = null) }
        }

        return main()
    }
}