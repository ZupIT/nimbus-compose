package br.zup.com.nimbus.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.zup.nimbus.core.tree.ServerDrivenNode

@Immutable
class ComponentData(
    val node: ServerDrivenNode,
    val parent: ServerDrivenNode?,
    val children: @Composable () -> Unit,
) {
    private val hash: Int = node.properties?.hashCode() ?: 0

    override fun hashCode(): Int {
        return hash
    }

    override fun equals(other: Any?): Boolean {
        return if (other is ComponentData) other.hashCode() == hash else false
    }
}
