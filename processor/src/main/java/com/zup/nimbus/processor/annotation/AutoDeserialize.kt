package com.zup.nimbus.processor.annotation

import com.zup.nimbus.processor.model.DeserializationType

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class AutoDeserialize(val type: DeserializationType = DeserializationType.Component)
