package com.zup.nimbus.processor

import com.squareup.kotlinpoet.ClassName
import com.zup.nimbus.processor.old.PackageNames

object ClassNames {
    val Composable = ClassName(PackageNames.composeRuntime, "Composable")
    val Text = ClassName(PackageNames.composeMaterial, "Text")
    val Color = ClassName(PackageNames.composeGraphics, "Color")
    val ComponentData = ClassName(PackageNames.nimbusCompose, "ComponentData")
    val NimbusTheme = ClassName(PackageNames.nimbusCompose, "NimbusTheme")
    val NimbusMode = ClassName(PackageNames.nimbusCompose, "NimbusMode")
    val AnyServerDrivenData = ClassName(
        "${PackageNames.nimbusCore}.deserialization",
        "AnyServerDrivenData",
    )
    val EntityDeserializer = ClassName(
        "${PackageNames.nimbusCompose}.deserialization",
        "EntityDeserializer",
    )
    val ActionTriggeredEvent = ClassName(PackageNames.nimbusCore, "ActionTriggeredEvent")
    val DeserializationContext = ClassName(
        "${PackageNames.nimbusCompose}.deserialization",
        "DeserializationContext",
    )
    val DeserializationError = ClassName(
        "${PackageNames.nimbusCompose}.deserialization",
        "DeserializationError",
    )
}