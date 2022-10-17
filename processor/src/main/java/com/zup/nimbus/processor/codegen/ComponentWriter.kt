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
        val fnBuilder = FunSpec.builder(component.simpleName.asString())
            .addAnnotation(ClassNames.Composable)
            .addParameter("component", ClassNames.ComponentData)
            .addStatement("val context = DeserializationContext(component)")
            .addStatement("val properties = AnyServerDrivenData(component.node.properties)")
            .addStatement("val nimbus = NimbusTheme.nimbus")
        val properties = component.parameters.map { Property.fromParameter(it) }
        val result = FunctionWriter(properties, deserializers, fnBuilder).write()
        fnBuilder.addCode(
            """
            |if (!properties.hasError()) {
            |  %L(
            |    %L
            |  )
            |} else if (nimbus.mode == NimbusMode.Development) {
            |   Text("Error while deserializing component.", color = Color.Red)
            |}
            """.trimMargin(),
            component.simpleName.asString(),
            FunctionCaller.buildParameterAssignments(properties).joinToString(",\n    ")
        )
        return result.combine(imports)
    }
}
