package br.com.zup.nimbus.processor.deserialization

import br.com.zup.nimbus.processor.ComponentData
import br.com.zup.nimbus.core.ActionTriggeredEvent

class DeserializationContext(
    val component: ComponentData? = null,
    val event: ActionTriggeredEvent? = null,
)
