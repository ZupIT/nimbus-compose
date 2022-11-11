package br.zup.com.nimbus.compose

import androidx.compose.runtime.Composable
import com.zup.nimbus.core.tree.ServerDrivenNode

private fun componentListsAreEqual(list: List<*>, comparable: List<*>): Boolean {
    return list.size == comparable.size && list.toSet() == comparable.toSet()
}

private fun componentMapsAreEqual(map: Map<String, *>, comparable: Map<String, *>): Boolean {
    val requiresDeepComparison = map.size == comparable.size && map.keys == comparable.keys
    if (requiresDeepComparison) {
        var areEqual = true
        for (entry in map.iterator()) {
            areEqual = when (entry.value) {
                is Function<*> -> continue
                is Map<*, *> -> componentMapsAreEqual(
                    entry.value as Map<String, *>,
                    comparable[entry.key] as Map<String, *>
                )
                is Array<*> -> (entry.value as Array<*>).contentEquals(comparable[entry.key] as Array<*>)
                is List<*> -> componentListsAreEqual(entry.value as List<*>, comparable as List<*>)
                else -> entry.value == comparable[entry.key]
            }
            if (!areEqual) break
        }
        return areEqual
    }
    return false
}

private fun componentsAreEquals(node: ServerDrivenNode, comparable: ServerDrivenNode): Boolean {
    return !(
        node.id != comparable.id ||
        node.component != comparable.component ||
        !(
            (node.properties == comparable.properties) || (
                node.properties?.let { otherProperties ->
                    comparable.properties?.let { currentProperties ->
                        componentMapsAreEqual(otherProperties, currentProperties)
                    }
                } == true
            )
        ) ||
        !(
            (node.children == comparable.children) || (
                node.children?.let { otherChildren ->
                    comparable.children?.let { currentChildren ->
                        (otherChildren.map { it.id }).toTypedArray()
                            .contentEquals((currentChildren.map { it.id }).toTypedArray())
                    }
                } == true
            )
        )
    )
}

class ComponentData(
    val node: ServerDrivenNode,
    val children: @Composable () -> Unit,
)
