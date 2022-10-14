package com.zup.nimbus.processor.codegen

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.zup.nimbus.processor.error.ErrorMessages
import com.zup.nimbus.processor.error.NoQualifiedName
import com.zup.nimbus.processor.error.RootPropertyMustBeDeserializable
import com.zup.nimbus.processor.model.FunctionWriterResult
import com.zup.nimbus.processor.model.Property
import com.zup.nimbus.processor.model.PropertyCategory
import com.zup.nimbus.processor.utils.getQualifiedName
import com.zup.nimbus.processor.utils.getSimpleName
import com.zup.nimbus.processor.utils.isEnum
import com.zup.nimbus.processor.utils.isList
import com.zup.nimbus.processor.utils.isMap
import com.zup.nimbus.processor.utils.isPrimitive
import com.zup.nimbus.processor.utils.resolveListType
import com.zup.nimbus.processor.utils.resolveMapType

class FunctionParameterWriter(
    private val parameters: List<Property>,
    private val deserializers: List<KSFunctionDeclaration>,
    private val fnBuilder: FunSpec.Builder,
) {
    private val typesToImport = mutableSetOf<ClassName>()
    private val typesToAutoDeserialize = mutableSetOf<KSType>()

    private fun getDeserializer(type: KSType): KSFunctionDeclaration? {
        return deserializers.find {
            it.returnType?.resolve()?.getQualifiedName() == type.getQualifiedName()
        }
    }

    private fun propertyToDeserializerKeys(property: Property): String {
        return getDeserializer(property.type)?.parameters?.joinToString(", ") {
            "\"${it.name}\""
        } ?: ""
    }

    private fun createItemOfType(type: KSType, itemRef: String): String {
        val nullable = if (type.isMarkedNullable) "OrNull" else ""
        val deserializer = getDeserializer(type)
        return when {
            deserializer != null -> """
                |${deserializer.simpleName}(${itemRef}, context) ?: run {
                |  ${itemRef}.errors.add("Unexpected null value at $\{${itemRef}.path}")
                |  null
                |}
                """".trimMargin()
            type.isPrimitive() -> "${itemRef}.as${type.getSimpleName()}${nullable}()"
            type.isEnum() -> "${itemRef}.asEnum${nullable}(${type.getSimpleName()}.values)"
            type.isList() -> "${itemRef}.asList${nullable}().map { " +
                    "${createItemOfType(type.resolveListType(), "it")} }"
            type.isMap() -> "${itemRef}.asMap${nullable}().mapValues { " +
                    "${createItemOfType(type.resolveMapType(), "it.value")} }"
            else -> "it.value"
        }
    }

    private fun writeComposable(property: Property) {
        if (property.type.isMarkedNullable) {
            fnBuilder.addStatement("val %L = context.component?.children", property.name)
        } else {
            fnBuilder.addCode("""
                    |val %L = context.component?.children ?: run {
                    |  properties.errors.add(%S)
                    |  null
                    |}
                    """.trimMargin(),
                property.name,
                ErrorMessages.noChildren,
            )
        }
    }

    private fun writeRootProperty(property: Property) {
        val deserializer = getDeserializer(property.type)
            ?: throw RootPropertyMustBeDeserializable()
        if (property.type.isMarkedNullable) {
            fnBuilder.addCode(
                """
                    |val %L = run {
                    |  val propertyNames = listOf(%L)
                    |  if (properties.hasAnyOf(propertyNames)) %L(properties, context)
                    |  else null
                    |}
                    """.trimMargin(),
                property.name,
                propertyToDeserializerKeys(property),
                deserializer.simpleName
            )
        } else {
            fnBuilder.addCode(
                """
                    |val %L = %L(properties, context) ?: run {
                    |  properties.errors.add(%S)
                    |  null
                    |}
                    """.trimMargin(),
                property.name,
                deserializer.simpleName,
                ErrorMessages.couldNotBuildProperty,
            )
        }
    }

    private fun writeCommonProperty(property: Property) {
        val deserializer = getDeserializer(property.type)
        when {
            deserializer != null -> {
                fnBuilder.addStatement(
                    "val %L = %L(properties.get(%S)%L)",
                    property.name,
                    deserializer.simpleName,
                    property.alias,
                    if (deserializer.parameters.size == 1) "" else ", context"
                )
            }
            property.type.isPrimitive() -> fnBuilder.addStatement(
                "val %L = properties.get(%L).as%L%L()",
                property.name,
                property.alias,
                property.type.getSimpleName(),
                if (property.type.isMarkedNullable) "OrNull" else "",
            )
            property.type.isEnum() -> fnBuilder.addStatement(
                "val %L = properties.get(%L).asEnum%L(%L.values)",
                property.name,
                property.alias,
                if (property.type.isMarkedNullable) "OrNull" else "",
                property.type.declaration.simpleName,
            )
            // todo: verify arity and return type (consider type aliases?)
            property.type.isFunctionType -> fnBuilder.addStatement(
                "val %L = properties.get(%L).asEvent%L()",
                property.name,
                property.alias,
                if (property.type.isMarkedNullable) "OrNull" else "",
            )
            property.type.isList() -> fnBuilder.addStatement(
                "val %L = properties.get(%S).asList%L()?.map { %L }",
                property.name,
                property.alias,
                if (property.type.isMarkedNullable) "OrNull" else "",
                createItemOfType(property.type.resolveListType(), "it"),
            )
            property.type.isMap() -> fnBuilder.addStatement(
                "val %L = properties.get(%S).asMap%L()?.mapValues { %L }",
                property.name,
                property.alias,
                if (property.type.isMarkedNullable) "OrNull" else "",
                createItemOfType(property.type.resolveMapType(), "it.value"),
            )
            else -> {
                typesToAutoDeserialize.add(property.type)
                fnBuilder.addStatement(
                    "val %L = EntityDeserializer.%L(properties.get(%S), context)",
                    property.name,
                    property.type.getQualifiedName()?.replace(".", "_")
                        ?: throw NoQualifiedName(property.type.getSimpleName()),
                    property.alias,
                )
            }
        }
    }

    private fun writeProperty(property: Property) {
        when (property.category) {
            PropertyCategory.Composable -> writeComposable(property)
            PropertyCategory.Context -> {} // skip
            PropertyCategory.Root -> writeRootProperty(property)
            PropertyCategory.Common -> writeCommonProperty(property)
        }
    }

    fun write(): FunctionWriterResult {
        parameters.forEach { writeProperty(it) }
        return FunctionWriterResult(typesToImport, typesToAutoDeserialize, fnBuilder)
    }
}