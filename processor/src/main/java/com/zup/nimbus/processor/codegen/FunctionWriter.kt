package com.zup.nimbus.processor.codegen

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.zup.nimbus.processor.error.ErrorMessages
import com.zup.nimbus.processor.error.IncompatibleCustomDeserializer
import com.zup.nimbus.processor.error.UndeserializableEntity
import com.zup.nimbus.processor.model.FunctionWriterResult
import com.zup.nimbus.processor.model.IdentifiableKSType
import com.zup.nimbus.processor.model.Property
import com.zup.nimbus.processor.model.PropertyCategory
import com.zup.nimbus.processor.utils.getPackageName
import com.zup.nimbus.processor.utils.getQualifiedName
import com.zup.nimbus.processor.utils.getSimpleName
import com.zup.nimbus.processor.utils.isEnum
import com.zup.nimbus.processor.utils.isList
import com.zup.nimbus.processor.utils.isMap
import com.zup.nimbus.processor.utils.isPrimitive
import com.zup.nimbus.processor.utils.resolveListType
import com.zup.nimbus.processor.utils.resolveMapType

class FunctionWriter(
    private val parameters: List<Property>,
    private val deserializers: List<KSFunctionDeclaration>,
    private val fnBuilder: FunSpec.Builder,
) {
    private val typesToImport = mutableSetOf<ClassName>()
    private val typesToAutoDeserialize = mutableSetOf<IdentifiableKSType>()

    private fun getDeserializer(type: KSType): KSFunctionDeclaration? {
        return deserializers.find {
            it.returnType?.resolve()?.getQualifiedName() == type.getQualifiedName()
        }
    }

    private fun callDeserializer(
        deserializer: KSFunctionDeclaration,
        propertiesRef: String = "properties",
        contextRef: String = "context",
    ): String {
        val name = deserializer.simpleName.asString()
        typesToImport.add(
            ClassName(deserializer.packageName.asString(), name)
        )
        val contextParam = if (deserializer.parameters.size == 1) "" else ", $contextRef"
        return "${name}(${propertiesRef}${contextParam})"
    }

    private fun propertyToDeserializerKeys(
        deserializer: KSFunctionDeclaration?,
        type: KSType,
    ): String {
        val params = if (deserializer != null) {
            deserializer.parameters
        } else {
            val declaration = type.declaration
            if (declaration is KSClassDeclaration) declaration.primaryConstructor?.parameters
            else null
        }
        return params?.joinToString(", ") {
            "\"${it.name?.asString()}\""
        } ?: ""
    }

    private fun createItemOfType(type: KSType, itemRef: String, origin: Property): String {
        val nullable = if (type.isMarkedNullable) "OrNull" else ""
        val optional = if (type.isMarkedNullable) "?" else ""
        val deserializer = getDeserializer(type)
        return when {
            deserializer != null -> {
                if (!type.isMarkedNullable &&
                    deserializer.returnType?.resolve()?.isMarkedNullable == true) {
                    throw IncompatibleCustomDeserializer(origin, deserializer)
                }
                callDeserializer(deserializer, itemRef)
            }
            type.isPrimitive() -> "${itemRef}.as${type.getSimpleName()}${nullable}()"
            type.isEnum() -> "${itemRef}.asEnum${nullable}(${type.getSimpleName()}.values)"
            type.isList() -> "${itemRef}.asList${nullable}()${optional}.map { " +
                    "${createItemOfType(type.resolveListType(), "it", origin)} }"
            type.isMap() -> "${itemRef}.asMap${nullable}()${optional}.mapValues { " +
                    "${createItemOfType(type.resolveMapType(), "it.value", origin)} }"
            else -> "${EntityWriter.createFunctionName(type)}(${itemRef}, context)"
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
        val call = if (deserializer == null) {
            typesToAutoDeserialize.add(IdentifiableKSType(property.type))
            val name = EntityWriter.createFunctionName(property.type)
            typesToImport.add(ClassName(property.type.getPackageName(), name))
            "$name(properties, context)"
        } else {
            if (!property.type.isMarkedNullable &&
                deserializer.returnType?.resolve()?.isMarkedNullable == true) {
                throw IncompatibleCustomDeserializer(property, deserializer)
            }
            callDeserializer(deserializer)
        }
        if (property.type.isMarkedNullable) {
            fnBuilder.addCode(
                """
                |val %L = run {
                |  val propertyNames = listOf(%L)
                |  if (properties.hasAnyOfKeys(propertyNames)) %L
                |  else null
                |}
                |
                """.trimMargin(),
                property.name,
                propertyToDeserializerKeys(deserializer, property.type),
                call,
            )
        } else {
            fnBuilder.addStatement("val %L = %L", property.name, call)
        }
    }

    private fun writeCommonProperty(property: Property) {
        val deserializer = getDeserializer(property.type)
        when {
            deserializer != null -> {
                if (!property.type.isMarkedNullable &&
                    deserializer.returnType?.resolve()?.isMarkedNullable == true) {
                    throw IncompatibleCustomDeserializer(property, deserializer)
                }
                fnBuilder.addStatement(
                    "val %L = %L",
                    property.name,
                    callDeserializer(deserializer, "properties.get(\"${property.alias}\")"),
                )
                typesToImport.add(ClassName(
                    deserializer.packageName.asString(),
                    deserializer.simpleName.asString(),
                ))
            }
            property.type.isPrimitive() -> fnBuilder.addStatement(
                "val %L = properties.get(%S).as%L%L()",
                property.name,
                property.alias,
                property.type.getSimpleName(),
                if (property.type.isMarkedNullable) "OrNull" else "",
            )
            property.type.isEnum() -> {
                fnBuilder.addStatement(
                    "val %L = properties.get(%S).asEnum%L(%L.values())",
                    property.name,
                    property.alias,
                    if (property.type.isMarkedNullable) "OrNull" else "",
                    property.type.getSimpleName(),
                )
                typesToImport.add(
                    ClassName(property.type.getPackageName(), property.type.getSimpleName())
                )
            }
            // todo: verify arity and return type (consider type aliases?)
            property.type.isFunctionType -> fnBuilder.addStatement(
                "val %L = properties.get(%S).asEvent%L()",
                property.name,
                property.alias,
                if (property.type.isMarkedNullable) "OrNull" else "",
            )
            property.type.isList() -> fnBuilder.addStatement(
                "val %L = properties.get(%S).asList%L()%L.map { %L }",
                property.name,
                property.alias,
                if (property.type.isMarkedNullable) "OrNull" else "",
                if (property.type.isMarkedNullable) "?" else "",
                createItemOfType(property.type.resolveListType(), "it", property),
            )
            property.type.isMap() -> fnBuilder.addStatement(
                "val %L = properties.get(%S).asMap%L()%L.mapValues { %L }",
                property.name,
                property.alias,
                if (property.type.isMarkedNullable) "OrNull" else "",
                if (property.type.isMarkedNullable) "?" else "",
                createItemOfType(property.type.resolveMapType(), "it.value", property),
            )
            else -> {
                // todo: treat entities with repeated names
                if (property.type.declaration.modifiers.contains(Modifier.SEALED)) {
                    throw UndeserializableEntity(
                        property.type,
                        "it's a sealed class",
                        property,
                    )
                }
                typesToAutoDeserialize.add(IdentifiableKSType(property.type))
                val deserializerName = EntityWriter.createFunctionName(property.type)
                typesToImport.add(ClassName(property.type.getPackageName(), deserializerName))
                if (property.type.isMarkedNullable) {
                    fnBuilder.addStatement(
                        "val %L = if (properties.containsKey(%S)) %L(properties.get(%S), context) else null",
                        property.name,
                        property.alias,
                        deserializerName,
                        property.alias,
                    )
                } else {
                    fnBuilder.addStatement(
                        "val %L = %L(properties.get(%S), context)",
                        property.name,
                        deserializerName,
                        property.alias,
                    )
                }
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