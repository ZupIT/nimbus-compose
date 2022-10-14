package com.zup.nimbus.processor.old

import kotlin.reflect.KClass

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER)
// FIXME: typing. should be: annotation class Computed<T: TypeDeserializer<*>>(val deserializer: KClass<T>)
annotation class Computed(val deserializer: KClass<*>)
