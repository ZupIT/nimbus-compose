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