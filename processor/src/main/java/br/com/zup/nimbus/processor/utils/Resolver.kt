package br.com.zup.nimbus.processor.utils

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import kotlin.reflect.KClass

/**
 * Finds functions in the source code annotated with the given annotation class.
 */
fun Resolver.findAnnotations(kClass: KClass<*>) =
    getSymbolsWithAnnotation(kClass.qualifiedName.toString())
        .filterIsInstance<KSFunctionDeclaration>()
