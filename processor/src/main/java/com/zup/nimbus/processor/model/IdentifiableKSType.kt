package com.zup.nimbus.processor.model

import com.google.devtools.ksp.symbol.KSType
import com.zup.nimbus.processor.utils.getQualifiedName
import com.zup.nimbus.processor.utils.getSimpleName

class IdentifiableKSType(private val value: KSType): KSType by value {
    override fun toString(): String = value.getQualifiedName() ?: value.getSimpleName()

    override fun equals(other: Any?): Boolean {
        return toString() == other.toString()
    }

    override fun hashCode(): Int {
        return toString().hashCode()
    }
}
