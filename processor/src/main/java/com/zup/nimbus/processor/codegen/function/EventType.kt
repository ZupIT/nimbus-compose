package com.zup.nimbus.processor.codegen.function

import com.google.devtools.ksp.symbol.KSType
import com.zup.nimbus.processor.codegen.ParameterUtils
import com.zup.nimbus.processor.codegen.function.FunctionWriter.PROPERTIES_REF
import com.zup.nimbus.processor.error.InvalidFunction
import com.zup.nimbus.processor.model.Property
import com.zup.nimbus.processor.utils.getQualifiedName
import com.zup.nimbus.processor.utils.getSimpleName
import com.zup.nimbus.processor.utils.isAny
import com.zup.nimbus.processor.utils.isEnum
import com.zup.nimbus.processor.utils.isKnown
import com.zup.nimbus.processor.utils.isList
import com.zup.nimbus.processor.utils.isMap
import com.zup.nimbus.processor.utils.isPrimitive

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
