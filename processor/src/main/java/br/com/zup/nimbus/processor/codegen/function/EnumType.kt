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

package br.com.zup.nimbus.processor.codegen.function

import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName
import br.com.zup.nimbus.processor.codegen.function.FunctionWriter.PROPERTIES_REF
import br.com.zup.nimbus.processor.utils.getPackageName
import br.com.zup.nimbus.processor.utils.getSimpleName

/**
 * Writes the code for deserializing a property of Enum type.
 */
internal object EnumType {
    fun getCallString(
        ctx: FunctionWriterContext,
        type: KSType = ctx.property.type,
        propertiesRef: String = PROPERTIES_REF,
    ): String {
        ctx.typesToImport.add(
            ClassName(type.getPackageName(), type.getSimpleName())
        )
        val nullable = if (type.isMarkedNullable) "OrNull" else ""
        return "$propertiesRef.asEnum$nullable(${type.getSimpleName()}.values())"
    }

    fun write(ctx: FunctionWriterContext) {
        ctx.builder.addStatement(
            "val %L = %L",
            ctx.property.name,
            getCallString(
                ctx,
                propertiesRef = ctx.property.getAccessString(PROPERTIES_REF),
            ),
        )
    }
}
