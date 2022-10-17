package com.zup.nimbus.processor.utils

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSAnnotated
import com.squareup.kotlinpoet.ClassName

@OptIn(KspExperimental::class)
inline fun <reified T: Annotation> KSAnnotated.getAnnotation(): T? {
    return this.getAnnotationsByType(T::class).firstOrNull()
}

@OptIn(KspExperimental::class)
inline fun <reified T: Annotation>KSAnnotated.hasAnnotation(): Boolean {
    return this.isAnnotationPresent(T::class)
}

fun KSAnnotated.hasAnnotation(className: ClassName): Boolean {
    return this.annotations.find {
        it.annotationType.resolve().declaration.qualifiedName?.asString() == className.canonicalName
    } != null
}
