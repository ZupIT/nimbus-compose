package com.zup.nimbus.processor.codegen

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.FunSpec
import com.zup.nimbus.processor.ClassNames
import com.zup.nimbus.processor.codegen.function.FunctionWriter
import com.zup.nimbus.processor.codegen.function.FunctionWriter.CONTEXT_REF
import com.zup.nimbus.processor.codegen.function.FunctionWriter.PROPERTIES_REF
import com.zup.nimbus.processor.model.FunctionWriterResult

internal object ComponentWriter {
    private const val COMPONENT_REF = "__component"

    private val imports = setOf(
        ClassNames.DeserializationContext,
        ClassNames.AnyServerDrivenData,
        ClassNames.NimbusTheme,
        ClassNames.NimbusMode,
        ClassNames.Text,
        ClassNames.Color,
    )

    fun write(
        component: KSFunctionDeclaration,
        deserializers: List<KSFunctionDeclaration>,
    ): FunctionWriterResult {
        val componentName = component.simpleName.asString()
        val fnBuilder = FunSpec.builder(componentName)
            .addAnnotation(ClassNames.Composable)
            .addParameter(COMPONENT_REF, ClassNames.ComponentData)
            .addStatement("val %L = DeserializationContext(%L)", CONTEXT_REF, COMPONENT_REF)
            .addStatement(
                "val %L = AnyServerDrivenData(%L.node.properties)",
                PROPERTIES_REF,
                COMPONENT_REF,
            )
        val properties = ParameterUtils.convertParametersIntoProperties(component.parameters)
        val result = FunctionWriter.write(properties, deserializers, fnBuilder)
        fnBuilder.addCode(
            """
            |if (!%L.hasError()) {
            |  %L(
            |    %L
            |  )
            |} else if (NimbusTheme.nimbus.mode == NimbusMode.Development) {
            |  NimbusTheme.nimbus.logger.error(
            |    "Can't deserialize properties of the component with id ${'$'}{%L.node.id} " +
            |            "into the composable %L. See the errors below:" +
            |            %L.errorsAsString()
            |  )
            |  Text("Error while deserializing component. Check the logs for details.", color = Color.Red)
            |}
            """.trimMargin(),
            PROPERTIES_REF,
            component.simpleName.asString(),
            ParameterUtils.buildParameterAssignments(properties).joinToString(",\n    "),
            COMPONENT_REF,
            componentName,
            PROPERTIES_REF,
        )
        return result.combine(imports)
    }
}
