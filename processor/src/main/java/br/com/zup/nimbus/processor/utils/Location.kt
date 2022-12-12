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

package br.com.zup.nimbus.processor.utils

import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.Location

private val fileNameRegex = Regex("[^/]+$")

/**
 * A location in the format (fileName:lineNumber). This can be read by IntelliJ and correctly linked
 * in the file system.
 */
fun Location.toLocationString(): String {
    if (this is FileLocation) {
        val match = fileNameRegex.find(this.filePath)?.groupValues?.firstOrNull()
        if (match != null) return "(${match}:${this.lineNumber})"
    }
    return ""
}
