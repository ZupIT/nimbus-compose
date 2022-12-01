package br.com.zup.nimbus.compose.deserialization

import br.com.zup.nimbus.compose.ComponentData
import br.com.zup.nimbus.core.ActionTriggeredEvent

class DeserializationContext(
    val component: ComponentData? = null,
    val event: ActionTriggeredEvent? = null,
)
