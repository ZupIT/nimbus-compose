package com.zup.nimbus.processor

import com.squareup.kotlinpoet.ClassName

object ClassNames {
    val Composable = ClassName(PackageNames.composeRuntime, "Composable")
    val Text = ClassName(PackageNames.composeMaterial, "Text")
    val Color = ClassName(PackageNames.composeGraphics, "Color")
    val ComponentData = ClassName(PackageNames.nimbusCompose, "ComponentData")
    val NimbusTheme = ClassName(PackageNames.nimbusCompose, "NimbusTheme")
    val NimbusMode = ClassName(PackageNames.nimbusCompose, "NimbusMode")
    val ComponentDeserializer = ClassName(
        "${PackageNames.nimbusCore}.deserialization",
        "ComponentDeserializer",
    )
}