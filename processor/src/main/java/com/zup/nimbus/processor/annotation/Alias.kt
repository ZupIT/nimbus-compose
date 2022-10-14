package com.zup.nimbus.processor.annotation

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Alias(val name: String)
