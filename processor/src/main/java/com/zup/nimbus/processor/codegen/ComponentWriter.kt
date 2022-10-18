package com.zup.nimbus.processor.codegen

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.FunSpec
import com.zup.nimbus.processor.ClassNames
import com.zup.nimbus.processor.model.FunctionWriterResult
import com.zup.nimbus.processor.model.Property

class ComponentWriter(
    private val component: KSFunctionDeclaration,
    private val deserializers: List<KSFunctionDeclaration>,
) {
    companion object {
        private val imports = setOf(
            ClassNames.DeserializationContext,
            ClassNames.AnyServerDrivenData,
            ClassNames.NimbusTheme,
            ClassNames.NimbusMode,
            ClassNames.Text,
            ClassNames.Color,
        )
    }

    fun write(): FunctionWriterResult {
        val componentName = component.simpleName.asString()
        val fnBuilder = FunSpec.builder(componentName)
            .addAnnotation(ClassNames.Composable)
            .addParameter("component", ClassNames.ComponentData)
            .addStatement("val context = DeserializationContext(component)")
            .addStatement("val properties = AnyServerDrivenData(component.node.properties)")
        val properties = component.parameters.map { Property.fromParameter(it) }
        val result = FunctionWriter(properties, deserializers, fnBuilder).write()
        fnBuilder.addCode(
            """
            |if (!properties.hasError()) {
            |  %L(
            |    %L
            |  )
            |} else if (NimbusTheme.nimbus.mode == NimbusMode.Development) {
            |  NimbusTheme.nimbus.logger.error(
            |    "Can't deserialize properties of the component with id ${'$'}{component.node.id} " +
            |            "into the composable $componentName. See the errors below:" +
            |            properties.errorsAsString()
            |  )
            |  Text("Error while deserializing component. Check the logs for details.", color = Color.Red)
            |}
            """.trimMargin(),
            component.simpleName.asString(),
            FunctionCaller.buildParameterAssignments(properties).joinToString(",\n    ")
        )
        return result.combine(imports)
    }
}
