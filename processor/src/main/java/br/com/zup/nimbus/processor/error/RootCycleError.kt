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

package br.com.zup.nimbus.processor.error

import br.br.com.zup.nimbus.processor.error.AutoDeserializationError
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import br.com.zup.nimbus.processor.utils.getSimpleName

internal class RootCycleError(param: KSValueParameter): AutoDeserializationError(
    "You can't have cycles when using the annotation @Root. Cyclic reference found in " +
            "the constructor of ${getSourceClass(param)} at the parameter named " +
            "${param.name?.asString() ?: "unknown"} of type ${param.type.resolve().getSimpleName()}.",
    param,
) {
    companion object {
        fun getSourceClass(param: KSValueParameter): String {
            val parent = param.parent?.parent
            return if (parent is KSClassDeclaration) parent.simpleName.asString() else "unknown"
        }
    }
}
