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

package br.com.zup.nimbus.processor

import com.squareup.kotlinpoet.ClassName

/**
 * The ClassNames (qualified names) of types often used by the generated code.
 */
object ClassNames {
    val Composable = ClassName(PackageNames.composeRuntime, "Composable")
    val Text = ClassName(PackageNames.composeMaterial, "Text")
    val Column = ClassName(PackageNames.composeLayout, "Column")
    val Color = ClassName(PackageNames.composeGraphics, "Color")
    val ComponentData = ClassName(PackageNames.nimbusCompose, "ComponentData")
    val Nimbus = ClassName(PackageNames.nimbusCompose, "Nimbus")
    val NimbusMode = ClassName(PackageNames.nimbusCompose, "NimbusMode")
    val AnyServerDrivenData = ClassName(
        "${PackageNames.nimbusCore}.deserialization",
        "AnyServerDrivenData",
    )
    val ActionTriggeredEvent = ClassName(PackageNames.nimbusCore, "ActionTriggeredEvent")
    val DeserializationContext = ClassName(
        "${PackageNames.nimbusCompose}.deserialization",
        "DeserializationContext",
    )
}