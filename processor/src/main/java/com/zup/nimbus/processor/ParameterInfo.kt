package com.zup.nimbus.processor

import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.ClassName
import kotlin.reflect.KClass

private val simpleTypes = listOf("String", "Int", "Double", "Boolean")

private fun isNimbusAction(type: KSType): Boolean {
    return type.isFunctionType &&
            (type.arguments.size == 1 || type.arguments.size == 2) &&
            type.arguments.last().type.toString() == "Unit"
}

class ParameterInfo(parameter: KSValueParameter, fn: KSFunctionDeclaration) {
    val name: String
    val type: String
    val nullable: Boolean
    val category: TypeCategory
    val packageName: String
    val mustDeserialize = mutableSetOf<KSClassDeclaration>()
    val isParentName: Boolean
    val deserializer: ClassName?
    val arity: Int?

    init {
        name = parameter.name?.asString() ?: throw NamelessParameterException(parameter, fn)
        type = parameter.type.toString()
        val resolved = parameter.type.resolve()
        nullable = resolved.isMarkedNullable
        packageName = resolved.declaration.packageName.asString()
        arity = resolved.arguments.size - 1 // - 1 because the return type is also in this array
        isParentName = parameter.annotations.any {
            // todo: don't rely on simple name
            annotation -> annotation.shortName.asString() == "ParentName"
        }
        val deserializerType = parameter.annotations.find {
            // todo: don't rely on simple name
            annotation -> annotation.shortName.asString() == "Computed"
        }?.arguments?.getOrNull(0)?.value as? KSType
        deserializer = deserializerType?.let {
            val declaration = it.declaration
            if (declaration !is KSClassDeclaration || declaration.classKind != ClassKind.OBJECT) {
                throw ComputedNotAnObjectException(parameter, fn)
            }
            ClassName(declaration.packageName.asString(), declaration.simpleName.asString())
        }

        category = if (simpleTypes.contains(type)) { // todo: don't rely on simple name
            TypeCategory.Primitive
        } else if (resolved.declaration.modifiers.contains(Modifier.ENUM)) {
            TypeCategory.Enum
        } else if (!resolved.isFunctionType && resolved.declaration is KSClassDeclaration) {
            val isRoot = parameter.annotations.any {
                // todo: don't rely on simple name
                annotation -> annotation.shortName.asString() == "Root"
            }
            if (!isRoot) throw NonRootEntityException(parameter, fn)
            mustDeserialize.add(resolved.declaration as KSClassDeclaration)
            TypeCategory.Deserializable
        } else if (parameter.type.annotations.find { it.shortName.asString() == "Composable" } != null) { // todo: don't rely on simple name
            TypeCategory.Composable
        } else if (isNimbusAction(resolved)) {
            TypeCategory.ServerDrivenAction
        } else {
            TypeCategory.Unknown
        }

        if (parameter.hasDefault && !nullable) throw DefaultParameterValueException(parameter, fn)
    }
}