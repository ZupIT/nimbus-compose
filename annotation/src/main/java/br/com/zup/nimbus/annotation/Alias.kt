package br.com.zup.nimbus.annotation

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Alias(val name: String)
