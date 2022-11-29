package br.com.zup.nimbus.processor

import androidx.compose.runtime.Composable
import com.zup.nimbus.core.tree.ServerDrivenNode

class ComponentData(
    val node: ServerDrivenNode,
    val children: @Composable () -> Unit,
)
