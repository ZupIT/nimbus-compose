package com.zup.nimbus.processor

import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.ClassName
import kotlin.reflect.KClass

private val simpleTypes = listOf("String", "Int", "Double", "Boolean")

class ParameterInfo(parameter: KSValueParameter, fn: KSFunctionDeclaration) {
    val name: String
    val type: String
    val nullable: Boolean
    val category: TypeCategory
    val packageName: String
    val mustDeserialize = mutableSetOf<KSClassDeclaration>()
    val isParentName: Boolean
    val deserializer: ClassName?

    init {
        name = parameter.name?.asString() ?: throw NamelessParameterException(parameter, fn)
        type = parameter.type.toString()
        val resolved = parameter.type.resolve()
        nullable = resolved.isMarkedNullable
        packageName = resolved.declaration.packageName.asString()
        isParentName = parameter.annotations.find {
            // todo: don't rely on simple name
            annotation -> annotation.shortName.asString() == "ParentName"
        } != null
        val deserializerType = parameter.annotations.find {
            // todo: don't rely on simple name
            annotation -> annotation.shortName.asString() == "Computed"
        }?.arguments?.getOrNull(0)?.value as? KSType
        deserializer = deserializerType?.let {
            ClassName(it.declaration.packageName.asString(), it.declaration.simpleName.asString())
        }

        category = if (simpleTypes.contains(type)) { // todo: don't rely on simple name
            TypeCategory.Primitive
        } else if (resolved.declaration.modifiers.contains(Modifier.ENUM)) {
            TypeCategory.Enum
        } else if (!resolved.isFunctionType && resolved.declaration is KSClassDeclaration) {
            mustDeserialize.add(resolved.declaration as KSClassDeclaration)
            TypeCategory.Deserializable
        } else if (parameter.type.annotations.find { it.shortName.asString() == "Composable" } != null) { // todo: don't rely on simple name
            TypeCategory.Composable
        } else if (resolved.isFunctionType) {  // todo: verify arity and return type (1, Unit)
            TypeCategory.ServerDrivenAction
        } else {
            TypeCategory.Unknown
        }

        if (parameter.hasDefault && !nullable) throw DefaultParameterValueException(parameter, fn)
    }
}