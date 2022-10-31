package br.com.zup.nimbus.compose.sample

import br.com.zup.nimbus.compose.sample.model.AdaptiveSize
import com.zup.nimbus.core.deserialization.AnyServerDrivenData
import br.com.zup.nimbus.annotation.AutoDeserialize

@AutoDeserialize
fun sum(a: Double, b: String, vararg other: List<AdaptiveSize?>, c: Double): Double {
    return 0.0
}
