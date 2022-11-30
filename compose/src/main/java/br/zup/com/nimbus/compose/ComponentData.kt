package br.zup.com.nimbus.compose

import androidx.compose.runtime.Composable
import br.com.zup.nimbus.core.tree.ServerDrivenNode

class ComponentData(
    val node: ServerDrivenNode,
    val children: @Composable () -> Unit,
    val childrenAsList: List<@Composable () -> Unit>,
)
