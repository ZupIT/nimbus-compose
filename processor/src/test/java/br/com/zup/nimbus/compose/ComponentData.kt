package br.com.zup.nimbus.compose

import androidx.compose.runtime.Composable
import br.com.zup.nimbus.core.tree.ServerDrivenNode

class ComponentData(
    val node: ServerDrivenNode,
    val children: @Composable () -> Unit,
)
