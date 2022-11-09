package com.zup.nimbus.processor.codegen

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.toTypeName
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
        val parent = component.parent
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
        // makes this function an extension of the parent element if the parent is a class or an
        // object
        if (parent is KSClassDeclaration) {
            fnBuilder.receiver(parent.asStarProjectedType().toTypeName())
        }
        val properties = ParameterUtils.convertParametersIntoNamedProperties(component.parameters)
        val result = FunctionWriter.write(properties, deserializers, fnBuilder)
        fnBuilder.addCode(
            """
            |if (!$PROPERTIES_REF.hasError()) {
            |  ${component.simpleName.asString()}(
            |    ${ParameterUtils.buildParameterAssignments(properties).joinToString(",\n    ")}
            |  )
            |} else if (NimbusTheme.nimbus.mode == NimbusMode.Development) {
            |  NimbusTheme.nimbus.logger.error(
            |    "Can't deserialize properties of the component with id ${'$'}{$COMPONENT_REF.node.id} " +
            |            "into the composable $componentName. See the errors below:" +
            |            $PROPERTIES_REF.errorsAsString()
            |  )
            |  Text("Error while deserializing component. Check the logs for details.", color = Color.Red)
            |}
            |""".trimMargin()
        )
        return result.combine(imports)
    }
}
