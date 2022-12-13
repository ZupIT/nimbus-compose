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

package test.utils

object Snippets {
    const val document = """        
        data class Document(
          val type: DocumentType,
          val value: String,
        )
    """

    const val documentType = """
        enum class DocumentType {
          CPF,
          RG,
          CNH,
        }
    """

    const val documentAndDocumentType = """
        $documentType
        $document
    """

    const val myDate = """
        class MyDate {
            var value: Long = 0
            override fun equals(other: Any?) = other is MyDate && value == other.value
            override fun toString() = "${'$'}value"
        }
        
        @Deserializer
        fun deserializeMyDate(data: AnyServerDrivenData): MyDate {
          val result = MyDate()
          result.value = data.asLongOrNull() ?: 0
          return result
        }
    """
}
