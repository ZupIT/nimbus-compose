package com.zup.nimbus.processor

import kotlin.reflect.KClass

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Computed<T: TypeDeserializer<*>>(val deserializer: KClass<T>)
