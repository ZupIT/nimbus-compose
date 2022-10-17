package com.zup.nimbus.processor.codegen

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.zup.nimbus.processor.ClassNames
import com.zup.nimbus.processor.error.UndeserializableEntity
import com.zup.nimbus.processor.model.FunctionWriterResult
import com.zup.nimbus.processor.model.Property
import com.zup.nimbus.processor.utils.getSimpleName

class EntityWriter(
    private val type: KSType,
    private val deserializers: List<KSFunctionDeclaration>,
) {
    companion object {
        fun createFunctionName(type: KSType): String {
            return "create${type.getSimpleName()}FromAnyServerDrivenData"
        }
    }

    private fun writeDeserializationFunction(
        className: ClassName,
        properties: List<Property>,
    ): FunctionWriterResult {
        val fnBuilder = FunSpec.builder(createFunctionName(type))
            .returns(className)
            .addParameter("properties", ClassNames.AnyServerDrivenData)
            .addParameter("context", ClassNames.DeserializationContext)
        val result = FunctionWriter(properties, deserializers, fnBuilder).write()
        fnBuilder.addCode(
            """
            |return %L(
            |  %L
            |)
            """.trimMargin(),
            className.simpleName,
            FunctionCaller.buildParameterAssignments(properties).joinToString(",\n  ")
        )
        return result
    }

    private fun writeClassDeserializer(declaration: KSClassDeclaration): FunctionWriterResult {
        val constructor = declaration.primaryConstructor ?: throw UndeserializableEntity(
            type, "this class doesn't have a public constructor"
        )
        val properties = constructor.parameters.map { Property.fromParameter(it) }
        val className = ClassName(
            declaration.packageName.asString(),
            declaration.simpleName.asString(),
        )
        return writeDeserializationFunction(className, properties)
    }

    fun write(): FunctionWriterResult {
        val declaration = type.declaration
        if (declaration is KSClassDeclaration) {
            return writeClassDeserializer(declaration)
        } else {
            throw UndeserializableEntity(type, "it's not a class")
        }
    }
}