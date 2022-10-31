package br.com.zup.nimbus.compose.sample.model

import com.zup.nimbus.core.deserialization.AnyServerDrivenData
import br.com.zup.nimbus.annotation.Deserializer

class Tree<T>(
    val value: T,
    val children: Tree<T>?,
)

@Deserializer
fun deserializeIntTree(data: AnyServerDrivenData): Tree<Int> {
    return Tree(10, null)
}

@Deserializer
fun deserializeStringTree(data: AnyServerDrivenData): Tree<String> {
    return Tree("hello", null)
}
