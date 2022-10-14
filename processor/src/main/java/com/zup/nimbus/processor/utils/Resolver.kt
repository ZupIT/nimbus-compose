package com.zup.nimbus.processor.utils

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import kotlin.reflect.KClass

fun Resolver.findAnnotations(kClass: KClass<*>) =
    getSymbolsWithAnnotation(kClass.qualifiedName.toString())
        .filterIsInstance<KSFunctionDeclaration>()
