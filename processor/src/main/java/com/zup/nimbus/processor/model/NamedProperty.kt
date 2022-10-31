package com.zup.nimbus.processor.model

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Location
import br.com.zup.nimbus.annotation.Alias
import com.zup.nimbus.processor.error.InvalidUseOfVararg
import com.zup.nimbus.processor.utils.getAnnotation

internal class NamedProperty(
    name: String,
    type: KSType,
    typeReference: KSTypeReference,
    category: PropertyCategory,
    location: Location,
    parent: KSFunctionDeclaration,
    val alias: String,
): Property(name, type, typeReference, category, location, parent) {
    companion object {
        fun fromParameter(param: KSValueParameter): NamedProperty {
            if (param.isVararg) throw InvalidUseOfVararg(param)
            val name = nameFromParameter(param)
            val type = typeFromParameter(param)
            return NamedProperty(
                name = name,
                alias = param.getAnnotation<Alias>()?.name ?: name,
                type = type,
                typeReference = param.type,
                category = categoryFromParam(param, type),
                location = param.location,
                parent = param.parent as KSFunctionDeclaration,
            )
        }
    }

    override fun getAccessString(ref: String): String = """$ref.get("$alias")"""

    override fun getContainsString(ref: String): String = """$ref.containsKey("$alias")"""
}
