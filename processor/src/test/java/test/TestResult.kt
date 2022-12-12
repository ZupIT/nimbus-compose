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

package test

import test.compiler.CompilationResult

interface GenericTestResult {
    fun get(): Any?
    fun getAll(): List<Any?>
    fun clear(): Unit
}

object TestResult: GenericTestResult {
    private var values = mutableListOf<Any?>()

    fun add(vararg value: Any?) = values.addAll(value)
    override fun get() = values.lastOrNull()
    override fun getAll(): List<Any?> = values
    override fun clear() {
        values = mutableListOf()
    }

    fun fromCompilation(compilation: CompilationResult): GenericTestResult {
        val clazz = compilation.loadClass(this::class.qualifiedName)

        return object: GenericTestResult {
            override fun get(): Any? =
                clazz.getDeclaredMethod("get").invoke(clazz.kotlin.objectInstance)
            override fun getAll() =
                clazz.getDeclaredMethod("getAll")
                    .invoke(clazz.kotlin.objectInstance) as List<Any?>
            override fun clear() {
                clazz.getDeclaredMethod("clear").invoke(clazz.kotlin.objectInstance)
            }
        }
    }
}

