package br.com.zup.nimbus.processor.model

import com.google.devtools.ksp.symbol.KSType
import br.com.zup.nimbus.processor.utils.getQualifiedName
import br.com.zup.nimbus.processor.utils.getSimpleName

/**
 * A KSType that can be identified by its type name. Useful for creating keys with a KSType.
 */
internal class IdentifiableKSType(private val value: KSType): KSType by value {
    override fun toString(): String = value.getQualifiedName() ?: value.getSimpleName()

    override fun equals(other: Any?): Boolean {
        return toString() == other.toString()
    }

    override fun hashCode(): Int {
        return toString().hashCode()
    }
}
