package br.zup.com.nimbus.compose.deserialization

import br.zup.com.nimbus.compose.ComponentData
import br.com.zup.nimbus.core.ActionTriggeredEvent

class DeserializationContext(
    val component: ComponentData? = null,
    val event: ActionTriggeredEvent? = null,
)
