package com.zup.nimbus.processor.codegen

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.zup.nimbus.processor.ClassNames
import com.zup.nimbus.processor.codegen.function.FunctionWriter
import com.zup.nimbus.processor.codegen.function.FunctionWriter.PROPERTIES_REF
import com.zup.nimbus.processor.model.FunctionWriterResult
import com.zup.nimbus.processor.model.Property
import com.zup.nimbus.processor.model.PropertyCategory

object OperationWriter {
    private const val ARGUMENTS_REF = "__arguments"

    private val imports = setOf(
        ClassNames.AnyServerDrivenData,
        ClassNames.NimbusMode,
    )

    fun write(
        operation: KSFunctionDeclaration,
        deserializers: List<KSFunctionDeclaration>,
    ): FunctionWriterResult {
        val actionName = operation.simpleName.asString()
        val properties = ParameterUtils.convertParametersIntoProperties(operation.parameters)
        val fnBuilder = FunSpec.builder(actionName)
            .addParameter(ARGUMENTS_REF, List::class.parameterizedBy(Any::class))
            .addStatement(
                "val %L = AnyServerDrivenData(%L)",
                PROPERTIES_REF,
                ARGUMENTS_REF,
            )
        val result = FunctionWriter.write(properties, deserializers, fnBuilder)
        fnBuilder.addCode(
            """
            |if (!%L.hasError()) {
            |  %L(
            |    %L
            |  )
            |} else if (%L.scope.nimbus.mode == NimbusMode.Development) {
            |  %L.scope.nimbus.logger.error(
            |    "Can't deserialize properties of the action ${'$'}{%L.action.name} in the event " +
            |            "${'$'}{%L.scope.name} of the component with id ${'$'}{%L.scope.node.id} " +
            |            "into the Action Handler %L. See the errors below:" +
            |            %L.errorsAsString()
            |  )
            |}
            """.trimMargin(),
            PROPERTIES_REF,
            operation.simpleName.asString(),
            ParameterUtils.buildParameterAssignments(properties).joinToString(",\n    "),
            ARGUMENTS_REF,
            ARGUMENTS_REF,
            ARGUMENTS_REF,
            ARGUMENTS_REF,
            ARGUMENTS_REF,
            actionName,
            PROPERTIES_REF,
        )

        return result.combine(imports)
    }
}
