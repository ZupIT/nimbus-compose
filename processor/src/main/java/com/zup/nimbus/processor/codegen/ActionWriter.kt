package com.zup.nimbus.processor.codegen

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.toTypeName
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
            if (it.type.hasAnnotation(ClassNames.Composable)) throw InvalidUseOfComposable(it)
        }
    }

    fun write(
        action: KSFunctionDeclaration,
        deserializers: List<KSFunctionDeclaration>,
    ): FunctionWriterResult {
        validate(action)
        val parent = action.parent
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
        // makes this function an extension of the parent element if the parent is a class or an
        // object
        if (parent is KSClassDeclaration) {
            fnBuilder.receiver(parent.asStarProjectedType().toTypeName())
        }
        val properties = ParameterUtils.convertParametersIntoNamedProperties(action.parameters)
        val result = FunctionWriter.write(properties, deserializers, fnBuilder)
        fnBuilder.addCode(
            """
            |if ($PROPERTIES_REF.hasError()) {
            |  throw IllegalArgumentException(
            |     "Can't deserialize properties of the action ${'$'}{$EVENT_REF.action.name} in the event " +
            |            "${'$'}{$EVENT_REF.scope.name} of the component with id ${'$'}{$EVENT_REF.scope.node.id} " +
            |            "into the Action Handler $actionName. See the errors below:" +
            |            $PROPERTIES_REF.errorsAsString()
            |  )
            |}
            |${action.simpleName.asString()}(
            |  ${ParameterUtils.buildParameterAssignments(properties).joinToString(",\n  ")}
            |)
            |""".trimMargin(),
        )

        return result.combine(imports)
    }
}
