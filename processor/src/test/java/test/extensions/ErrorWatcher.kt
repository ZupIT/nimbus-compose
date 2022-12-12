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

package test.extensions

import br.com.zup.nimbus.compose.MockLogger
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestWatcher
import test.compiler.CompilationResult
import test.compiler.TestCompiler

class ErrorWatcher: TestWatcher {
    companion object {
        private var lastCompilation: CompilationResult? = null
        private var lastLogErrors: List<String>? = null
        // Everything after this is in red
        private const val RED = "\u001b[31m"
    }

    override fun testFailed(extensionContext: ExtensionContext?, throwable: Throwable?) {
        val compilation = TestCompiler.lastCompilationResult
        val className = extensionContext?.testClass?.get()?.name ?: ""
        val methodName = extensionContext?.testMethod?.get()?.name ?: ""
        val testName = "$className: $methodName"
        compilation?.let {
            if (compilation !== lastCompilation && compilation.hasSomeError()) {
                val errorType = if (compilation.hasCompilationError()) "COMPILATION"
                else "ANNOTATION PROCESSOR"
                println("$RED=========================================================")
                println("$RED$errorType ERROR")
                println("$RED=========================================================")
                println("${RED}Attention: kotlin-compile-testing allows compilation errors to " +
                        "go through the annotation processor. This doesn't happen in real " +
                        "scenarios, just in testing. This is important to know because, despite " +
                        "the exception, this error might not be related to the Annotation " +
                        "Processor itself, but instead, it can be a simple problem in the source " +
                        "code provided to the function `TestCompiler.compile`.")
                println("Compiled at: ${compilation.directory}")
                println("At test class: $className")
                println("At test method: $methodName")
                println(compilation.output)
                println("$RED=========================================================\n")
            }
        }

        if(MockLogger.errors !== lastLogErrors && MockLogger.errors.isNotEmpty()) {
            println("$RED=========================================================")
            println("${RED}NIMBUS ERROR LOGS")
            println("$RED=========================================================")
            println("At test class: $className")
            println("At test method: $methodName")
            MockLogger.errors.forEach { println(it) }
            println("$RED=========================================================\n")
        }

        if (compilation?.hasSomeError() == true) println("${RED}There was a compilation error " +
                "while running the test `$testName`. The compilation logs are printed above.")
        if (MockLogger.errors.isNotEmpty()) println("${RED}Nimbus log errors have been generated " +
                "while running the test `$testName`. The Nimbus error logs are printed above.")

        PrepareAndClose.hasFailingTests = true
        lastCompilation = compilation
        lastLogErrors = MockLogger.errors
    }
}
