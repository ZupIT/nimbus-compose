/*
 * Copyright 2023 ZUP IT SERVICOS EM TECNOLOGIA E INOVACAO SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.zup.nimbus.processor.codegen

import br.com.zup.nimbus.processor.ClassNames
import br.com.zup.nimbus.processor.codegen.function.FunctionWriter.CONTEXT_REF
import br.com.zup.nimbus.processor.codegen.function.FunctionWriter.PROPERTIES_REF
import com.squareup.kotlinpoet.FunSpec
import br.com.zup.nimbus.processor.model.NamedProperty

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
        ClassNames.Nimbus,
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
            |} else if (Nimbus.instance.mode == NimbusMode.Development) {
            |  Nimbus.instance.logger.error(
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
