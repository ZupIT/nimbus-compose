package com.zup.nimbus.processor.codegen

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.FunSpec
import com.zup.nimbus.processor.ClassNames
import com.zup.nimbus.processor.codegen.function.FunctionWriter
import com.zup.nimbus.processor.codegen.function.FunctionWriter.CONTEXT_REF
import com.zup.nimbus.processor.codegen.function.FunctionWriter.PROPERTIES_REF
import com.zup.nimbus.processor.error.InvalidUseOfComposable
import com.zup.nimbus.processor.model.FunctionWriterResult
import com.zup.nimbus.processor.utils.hasAnnotation

internal object ActionWriter {
    private const val EVENT_REF = "__event"

    private val imports = setOf(
        ClassNames.DeserializationContext,
        ClassNames.AnyServerDrivenData,
    )

    private fun validate(action: KSFunctionDeclaration) {
        action.parameters.forEach {
            if (it.hasAnnotation(ClassNames.Composable)) throw InvalidUseOfComposable(it)
        }
    }

    fun write(
        action: KSFunctionDeclaration,
        deserializers: List<KSFunctionDeclaration>,
    ): FunctionWriterResult {
        validate(action)
        val actionName = action.simpleName.asString()
        val fnBuilder = FunSpec.builder(actionName)
            .addParameter(EVENT_REF, ClassNames.ActionTriggeredEvent)
            .addStatement("val %L = DeserializationContext(null, %L)",
                CONTEXT_REF,
                EVENT_REF
            )
            .addStatement(
                "val %L = AnyServerDrivenData(%L.action.properties)",
                PROPERTIES_REF,
                EVENT_REF,
            )
        val properties = ParameterUtils.convertParametersIntoNamedProperties(action.parameters)
        val result = FunctionWriter.write(properties, deserializers, fnBuilder)
        fnBuilder.addCode(
            """
            |if (!%L.hasError()) {
            |  %L(
            |    %L
            |  )
            |} else {
            |  %L.scope.nimbus.logger.error(
            |    "Can't deserialize properties of the action ${'$'}{%L.action.name} in the event " +
            |            "${'$'}{%L.scope.name} of the component with id ${'$'}{%L.scope.node.id} " +
            |            "into the Action Handler %L. See the errors below:" +
            |            %L.errorsAsString()
            |  )
            |}
            """.trimMargin(),
            PROPERTIES_REF,
            action.simpleName.asString(),
            ParameterUtils.buildParameterAssignments(properties).joinToString(",\n    "),
            EVENT_REF,
            EVENT_REF,
            EVENT_REF,
            EVENT_REF,
            actionName,
            PROPERTIES_REF,
        )

        return result.combine(imports)
    }
}
