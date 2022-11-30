package br.com.zup.nimbus.processor.codegen

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.FunSpec
import br.com.zup.nimbus.processor.ClassNames
import br.com.zup.nimbus.processor.codegen.function.FunctionWriter.CONTEXT_REF
import br.com.zup.nimbus.processor.codegen.function.FunctionWriter.PROPERTIES_REF
import br.com.zup.nimbus.processor.error.InvalidUseOfComposable
import br.com.zup.nimbus.processor.model.NamedProperty
import br.com.zup.nimbus.processor.utils.hasAnnotation

/**
 * Writes the code for any action handler annotated with @AutoDeserialize.
 *
 * Action handlers are identified by the rule: functions not annotated with @Composable that returns
 * Unit.
 */
internal object ActionWriter: ComponentActionWriter() {
    /**
     * Name of the variable that holds a reference to the `ActionTriggeredEvent`. It's prefixed with
     * "__" to avoid name clashes with user defined parameters (deserialized properties).
     */
    private const val EVENT_REF = "__event"

    override val imports = setOf(
        ClassNames.DeserializationContext,
        ClassNames.AnyServerDrivenData,
    )

    /**
     * Checks if any of the parameters is a composable function (invalid).
     */
    override fun validate(declaration: KSFunctionDeclaration) {
        declaration.parameters.forEach {
            if (it.type.hasAnnotation(ClassNames.Composable)) throw InvalidUseOfComposable(it)
        }
    }

    override fun writeHeader(name: String) = FunSpec.builder(name)
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

    override fun writeFooter(
        name: String,
        properties: List<NamedProperty>,
        fnBuilder: FunSpec.Builder,
    ) {
        fnBuilder.addCode(
            """
            |if ($PROPERTIES_REF.hasError()) {
            |  throw IllegalArgumentException(
            |     "Can't deserialize properties of the action ${'$'}{$EVENT_REF.action.name} in the event " +
            |            "${'$'}{$EVENT_REF.scope.name} of the component with id ${'$'}{$EVENT_REF.scope.node.id} " +
            |            "into the Action Handler $name. See the errors below:" +
            |            $PROPERTIES_REF.errorsAsString()
            |  )
            |}
            |$name(
            |  ${ParameterUtils.buildParameterAssignments(properties).joinToString(",\n  ")}
            |)
            |""".trimMargin(),
        )
    }
}
