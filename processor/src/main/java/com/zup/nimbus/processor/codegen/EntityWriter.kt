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

class EntityWriter(
    private val type: KSType,
    private val deserializers: List<KSFunctionDeclaration>,
) {
    private fun writeDeserializationFunction(
        className: ClassName,
        properties: List<Property>,
    ): FunctionWriterResult {
        val fnBuilder = FunSpec.builder(className.canonicalName.replace(".", "_"))
            .returns(className)
            .addParameter("properties", ClassNames.AnyServerDrivenData)
            .addParameter("context", ClassNames.DeserializationContext)
        val result = FunctionParameterWriter(properties, deserializers, fnBuilder).write()
        fnBuilder.addCode(
            """
                |return %L(
                |  %L
                |)
                """.trimMargin(),
            className.simpleName,
            FunctionCaller.buildParameterAssignments(properties).joinToString(",\n|  ")
        )
        return result
    }

    private fun writeClassDeserializer(declaration: KSClassDeclaration): FunctionWriterResult {
        val constructor = declaration.primaryConstructor ?: throw UndeserializableEntity(
            type, "this class doesn't have a public constructor"
        )
        val properties = constructor.parameters.map { Property.fromParameter(it) }
        val className = ClassName(
            declaration.packageName.getShortName(),
            declaration.simpleName.getShortName(),
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