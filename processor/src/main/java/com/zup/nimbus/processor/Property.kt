package com.zup.nimbus.processor

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class PropertyPath(val path: String)

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class RootProperty

