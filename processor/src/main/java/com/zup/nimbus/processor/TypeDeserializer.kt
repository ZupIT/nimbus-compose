package com.zup.nimbus.processor

interface TypeDeserializer<T> {
    // todo: Any should be ComponentData, but how to import it from nimbus-compose?
    fun deserialize(data: Any): T
}
