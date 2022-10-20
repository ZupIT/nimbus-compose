package com.zup.nimbus.processor.model

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Location

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

    override fun getContainsString(ref: String): String = "$ref.listSize() > $index"
}
