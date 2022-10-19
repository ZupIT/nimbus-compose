package com.zup.nimbus.processor.model

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Location
import com.zup.nimbus.processor.ClassNames
import com.zup.nimbus.processor.annotation.Alias
import com.zup.nimbus.processor.annotation.Root
import com.zup.nimbus.processor.error.InvalidUseOfRoot
import com.zup.nimbus.processor.error.NamelessProperty
import com.zup.nimbus.processor.utils.getAnnotation
import com.zup.nimbus.processor.utils.getQualifiedName
import com.zup.nimbus.processor.utils.hasAnnotation
import com.zup.nimbus.processor.utils.isKnown

class Property(
    val name: String,
    val alias: String,
    val type: KSType,
    val category: PropertyCategory,
    val location: Location,
    val parent: KSFunctionDeclaration,
) {
    companion object {
        private fun validateRoot(param: KSValueParameter, type: KSType) {
            if(type.isKnown()) throw InvalidUseOfRoot(param)
        }

        private fun validateNullability(param: KSValueParameter, name: String, type: KSType) {
            if(param.hasDefault && !type.isMarkedNullable) {
                println("Warning: getting default values via annotation processors is " +
                        "unsupported. Parameter with name $name at ${param.location} will be " +
                        "handled as required by the auto-deserialization.")
            }
        }

        fun fromParameter(param: KSValueParameter): Property {
            val name = param.name?.asString() ?: throw NamelessProperty(param)
            val type = param.type.resolve()
            validateNullability(param, name, type)
            val category = when {
                param.hasAnnotation<Root>() -> {
                    validateRoot(param, type)
                    PropertyCategory.Root
                }
                param.type.resolve().getQualifiedName()
                        == ClassNames.DeserializationContext.canonicalName -> PropertyCategory.Context
                param.type.hasAnnotation(ClassNames.Composable) -> PropertyCategory.Composable
                else -> PropertyCategory.Common
            }
            return Property(
                name = name,
                alias = param.getAnnotation<Alias>()?.name ?: name,
                type = type,
                category = category,
                location = param.location,
                parent = param.parent as KSFunctionDeclaration,
            )
        }
    }
}