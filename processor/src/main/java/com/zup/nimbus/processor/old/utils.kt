package com.zup.nimbus.processor.old

import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.Modifier

private val simpleTypes = listOf("String", "Int", "Double", "Boolean")

object Utils {
    fun isSimpleType(type: KSTypeReference): Boolean {
        return simpleTypes.contains(type.toString())
    }

    fun isEnum(type: KSType): Boolean {
        return type.declaration.modifiers.contains(Modifier.ENUM)
    }
}