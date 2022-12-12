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

package br.br.com.zup.nimbus.processor.error

import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Location
import br.com.zup.nimbus.processor.model.Property
import br.com.zup.nimbus.processor.utils.getQualifiedName
import br.com.zup.nimbus.processor.utils.toLocationString

internal open class AutoDeserializationError(
    message: String,
    declaration: KSFunctionDeclaration? = null,
    location: Location? = null,
): Error(
    message +
            if (declaration == null || location == null) ""
            else getInfo(declaration, location)
) {
    constructor(message: String, property: Property? = null): this(
        message,
        property?.parent,
        property?.location,
    )

    constructor(message: String, parameter: KSValueParameter?): this(
        message,
        parameter?.parent as? KSFunctionDeclaration,
        parameter?.location,
    )

    companion object {
        private fun getClassOfConstructor(fn: KSFunctionDeclaration): String? {
            return if (fn.isConstructor()) fn.returnType?.resolve()?.getQualifiedName()
            else null
        }

        private fun getQualifiedNameForParameter(declaration: KSFunctionDeclaration): String {
            return declaration.qualifiedName?.asString()
                ?: getClassOfConstructor(declaration)
                ?: declaration.simpleName.asString()
        }

        private fun getInfo(declaration: KSFunctionDeclaration, location: Location): String {
            return "\n\tparameter at " +
                    getQualifiedNameForParameter(declaration) +
                    location.toLocationString()
        }
  }
}
