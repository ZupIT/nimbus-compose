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

package br.com.zup.nimbus.processor.model

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Location

/**
 * A type of property that can be accessed through its index.
 */
internal class IndexedProperty(
    name: String,
    type: KSType,
    typeReference: KSTypeReference,
    category: PropertyCategory,
    location: Location,
    parent: KSFunctionDeclaration,
    val index: Int,
    val isVararg: Boolean,
): Property (name, type, typeReference, category, location, parent) {
    companion object {
        fun fromParameter(param: KSValueParameter, index: Int): IndexedProperty {
            val type = typeFromParameter(param)
            return IndexedProperty(
                name = nameFromParameter(param),
                type = type,
                typeReference = param.type,
                category = categoryFromParam(param, type),
                location = param.location,
                parent = param.parent as KSFunctionDeclaration,
                index = index,
                isVararg = param.isVararg
            )
        }
    }

    override fun getAccessString(ref: String): String = "$ref.at($index)"

    override fun getContainsString(ref: String): String = "$ref.hasValueForIndex($index)"
}
