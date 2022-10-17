package br.com.zup.nimbus.compose.sample.model

import com.zup.nimbus.core.deserialization.AnyServerDrivenData
import com.zup.nimbus.processor.annotation.Deserializer

sealed class AdaptiveSize {
    object Expand : AdaptiveSize()
    object FitContent : AdaptiveSize()
    class Fixed(val value: Double): AdaptiveSize()
}

@Deserializer
fun deserializeAdaptiveSize(size: AnyServerDrivenData): AdaptiveSize? {
    val str = size.asStringOrNull()?.lowercase()
    if (str === "expand") return AdaptiveSize.Expand
    if (str === "fitcontent") return AdaptiveSize.FitContent
    val double = size.asDoubleOrNull()
    if (double != null) return AdaptiveSize.Fixed(double)
    return null
}
