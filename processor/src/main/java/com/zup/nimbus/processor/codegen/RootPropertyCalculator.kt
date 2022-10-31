package com.zup.nimbus.processor.codegen

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import br.com.zup.nimbus.annotation.Alias
import br.com.zup.nimbus.annotation.Ignore
import br.com.zup.nimbus.annotation.Root
import com.zup.nimbus.processor.codegen.function.CustomDeserialized
import com.zup.nimbus.processor.utils.getAnnotation
import com.zup.nimbus.processor.utils.getQualifiedName
import com.zup.nimbus.processor.utils.hasAnnotation

object RootPropertyCalculator {
    private val results = mutableMapOf<String, List<String>>()

    private fun getName(param: KSValueParameter) =
        param.getAnnotation<Alias>()?.name ?: param.name?.asString() ?: ""

    private fun calculateAllParamsInFunction(
        fn: KSFunctionDeclaration,
        deserializers: List<KSFunctionDeclaration>,
    ): List<String>  {
        val result = mutableListOf<String>()
        fn.parameters.forEach { param ->
            if (param.hasAnnotation<Root>()) {
                val paramType = param.type.resolve()
                val deserializer = CustomDeserialized.findDeserializer(paramType, deserializers)
                result.addAll(
                    deserializer?.parameters?.mapNotNull { it.name?.asString() }
                        ?: getAllParamsInTypeConstructor(paramType, deserializers)
                )
            } else if (!param.hasAnnotation<Ignore>()) result.add(getName(param))
        }
        return result.distinct()
    }

    private fun calculateAllParamsInTypeConstructor(
        type: KSType,
        deserializers: List<KSFunctionDeclaration>,
    ) {
        val declaration = type.declaration
        val result = if (declaration is KSClassDeclaration) {
            val constructor = declaration.primaryConstructor
            constructor?.let { calculateAllParamsInFunction(it, deserializers) } ?: emptyList()
        } else emptyList()
        type.getQualifiedName()?.let { results[it] = result }
    }

    fun getAllParamsInTypeConstructor(
        type: KSType,
        deserializers: List<KSFunctionDeclaration>,
    ): List<String> {
        val name = type.getQualifiedName()
        if (!results.containsKey(name)) calculateAllParamsInTypeConstructor(type, deserializers)
        return results[name]!!
    }
}
