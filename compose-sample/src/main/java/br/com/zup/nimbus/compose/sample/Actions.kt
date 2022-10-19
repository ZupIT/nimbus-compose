package br.com.zup.nimbus.compose.sample

import br.com.zup.nimbus.compose.sample.model.AdaptiveSize
import br.zup.com.nimbus.compose.deserialization.DeserializationContext
import com.zup.nimbus.processor.annotation.AutoDeserialize
import com.zup.nimbus.processor.annotation.Root
import com.zup.nimbus.processor.model.DeserializationType

@AutoDeserialize(DeserializationType.Action)
fun myAction1(
    str: String,
    size: AdaptiveSize?,
    names: List<String>,
    age: Int,
    onSuccess: (data: Map<String, Any>) -> Unit,
    anyList: List<Any>,
    anyMap: Map<String, Any?>,
    any: Any?,
    ctx: DeserializationContext,
) {
    print("MyAction1")
}

class MyActionParams(
    str: String,
    size: AdaptiveSize?,
    names: List<String>,
    age: Int,
    onSuccess: (data: Map<String, Any>) -> Unit,
    anyList: List<Any>,
    anyMap: Map<String, Any?>,
    any: Any?,
    ctx: DeserializationContext,
)

@AutoDeserialize(DeserializationType.Action)
fun myAction2(@Root params: MyActionParams) {
    print("MyAction2")
}
