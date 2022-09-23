package br.zup.com.nimbus.compose

import com.zup.nimbus.core.deserialization.ComponentDeserializer

interface TypeDeserializer<T> {
    fun deserialize(properties: ComponentDeserializer, data: ComponentData, name: String): T
}
