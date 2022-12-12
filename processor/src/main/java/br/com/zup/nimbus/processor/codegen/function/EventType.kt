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
import br.com.zup.nimbus.processor.codegen.function.FunctionWriter.PROPERTIES_REF
import br.com.zup.nimbus.processor.error.InvalidFunction
import br.com.zup.nimbus.processor.model.Property
import br.com.zup.nimbus.processor.utils.getQualifiedName
import br.com.zup.nimbus.processor.utils.isAny
import br.com.zup.nimbus.processor.utils.isList
import br.com.zup.nimbus.processor.utils.isMap
import br.com.zup.nimbus.processor.utils.isPrimitive

/**
 * Writes the code for deserializing a property that represents a Server Driven Event. Events are
 * represented by the types `() -> Unit` or `(T) -> Unit`, where T is any primitive type and
 * represents the value of the state associated to the event.
 */
internal object EventType {
    private const val PARAM_NAME = "stateValue"

    private fun returnsUnit(type: KSType): Boolean {
        val returnType = type.arguments.lastOrNull()
        return returnType?.type?.resolve()?.getQualifiedName() == Unit::class.qualifiedName
    }

    private fun isValidMap(type: KSType): Boolean {
        if (!type.isMap()) return false
        val keyType = type.arguments.firstOrNull()?.type?.resolve()
        val valueType = type.arguments.getOrNull(1)?.type?.resolve()
        return keyType?.getQualifiedName() == String::class.qualifiedName &&
                isTypeValid(valueType)
    }

    private fun isValidList(type: KSType): Boolean {
        if (!type.isList()) return false
        val valueType = type.arguments.firstOrNull()?.type?.resolve()
        return isTypeValid(valueType)
    }

    private fun isTypeValid(type: KSType?): Boolean {
        return type?.let {
                t -> t.isAny() || t.isPrimitive() || isValidMap(t) || isValidList(t)
        } ?: false
    }

    private fun isParameterValid(type: KSType): Boolean {
        if (type.arguments.size == 1) return true
        val firstParam = type.arguments.first()
        val resolved = firstParam.type?.resolve()
        return isTypeValid(resolved)
    }

    private fun getNumberOfParametersAndValidate(property: Property): Int {
        /* The type FunctionX has X + 1 type arguments (generics), so the number of parameters of
        the function is the total number of type arguments - 1. */
        val numberOfParams = property.type.arguments.size - 1
        if (numberOfParams > 1 || !returnsUnit(property.type) || !isParameterValid(property.type)) {
            throw InvalidFunction(property)
        }
        return numberOfParams
    }

    private fun writeNullableEventCall(ctx: FunctionWriterContext, numberOfParams: Int) {
        ctx.builder.addStatement(
            "val %L = __%LEvent?.let { ev -> { %Lev.run(%L) } }",
            ctx.property.name,
            ctx.property.name,
            if (numberOfParams == 0) "" else "$PARAM_NAME: Any? -> ",
            if (numberOfParams == 0) "" else PARAM_NAME,
        )
    }

    private fun writeNonNullableEventCall(ctx: FunctionWriterContext, numberOfParams: Int) {
        ctx.builder.addStatement(
            "val %L = { %L__%LEvent.run(%L) }",
            ctx.property.name,
            if (numberOfParams == 0) "" else "$PARAM_NAME: Any? -> ",
            ctx.property.name,
            if (numberOfParams == 0) "" else PARAM_NAME,
        )
    }

    fun write(ctx: FunctionWriterContext) {
        val numberOfParams = getNumberOfParametersAndValidate(ctx.property)
        ctx.builder.addStatement(
            "val __%LEvent = %L.asEvent%L()",
            ctx.property.name,
            ctx.property.getAccessString(PROPERTIES_REF),
            if (ctx.property.type.isMarkedNullable) "OrNull" else "",
        )
        if (ctx.property.type.isMarkedNullable) writeNullableEventCall(ctx, numberOfParams)
        else writeNonNullableEventCall(ctx, numberOfParams)
    }
}
