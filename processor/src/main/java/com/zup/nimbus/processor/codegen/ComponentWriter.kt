package com.zup.nimbus.processor.codegen

import com.squareup.kotlinpoet.FunSpec
import com.zup.nimbus.processor.ClassNames
import com.zup.nimbus.processor.codegen.function.FunctionWriter.CONTEXT_REF
import com.zup.nimbus.processor.codegen.function.FunctionWriter.PROPERTIES_REF
import com.zup.nimbus.processor.model.NamedProperty

/**
 * Writes the code for any component annotated with @AutoDeserialize.
 *
 * Components are identified by the use of the annotation @Composable from Android.
 */
internal object ComponentWriter: ComponentActionWriter() {
    /**
     * Name of the variable that holds a reference to the `ComponentData`. It's prefixed with
     * "__" to avoid name clashes with user defined parameters (deserialized properties).
     */
    private const val COMPONENT_REF = "__component"

    override val imports = setOf(
        ClassNames.DeserializationContext,
        ClassNames.AnyServerDrivenData,
        ClassNames.NimbusTheme,
        ClassNames.NimbusMode,
        ClassNames.Text,
        ClassNames.Color,
    )

    override fun writeHeader(name: String) = FunSpec.builder(name)
            .addAnnotation(ClassNames.Composable)
            .addParameter(COMPONENT_REF, ClassNames.ComponentData)
            .addStatement("val %L = DeserializationContext(%L)", CONTEXT_REF, COMPONENT_REF)
            .addStatement(
                "val %L = AnyServerDrivenData(%L.node.properties)",
                PROPERTIES_REF,
                COMPONENT_REF,
            )

    override fun writeFooter(
        name: String,
        properties: List<NamedProperty>,
        fnBuilder: FunSpec.Builder,
    ) {
        fnBuilder.addCode(
            """
            |if (!$PROPERTIES_REF.hasError()) {
            |  $name(
            |    ${ParameterUtils.buildParameterAssignments(properties).joinToString(",\n    ")}
            |  )
            |} else if (NimbusTheme.nimbus.mode == NimbusMode.Development) {
            |  NimbusTheme.nimbus.logger.error(
            |    "Can't deserialize properties of the component with id ${'$'}{$COMPONENT_REF.node.id} " +
            |            "into the composable $name. See the errors below:" +
            |            $PROPERTIES_REF.errorsAsString()
            |  )
            |  Text("Error while deserializing component. Check the logs for details.", color = Color.Red)
            |}
            |""".trimMargin()
        )
    }
}
