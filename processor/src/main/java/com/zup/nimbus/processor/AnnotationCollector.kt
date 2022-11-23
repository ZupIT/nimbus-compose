package com.zup.nimbus.processor

import br.com.zup.nimbus.annotation.AutoDeserialize
import br.com.zup.nimbus.annotation.Deserializer
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.zup.nimbus.processor.error.InvalidDeserializerParameters
import com.zup.nimbus.processor.model.DeserializableFunction
import com.zup.nimbus.processor.model.FunctionCategory
import com.zup.nimbus.processor.utils.findAnnotations
import com.zup.nimbus.processor.utils.getQualifiedName
import com.zup.nimbus.processor.utils.hasAnnotation
import com.zup.nimbus.processor.utils.isUnit

/**
 * Collects annotations in the source code. Also performs some validations.
 */
object AnnotationCollector {
    private val validDeserializerTypes = listOf(
        ClassNames.AnyServerDrivenData.canonicalName,
        ClassNames.DeserializationContext.canonicalName,
    )

    /**
     * Validates if the deserialization function has a correct set of parameters:
     * `(AnyServerDrivenData)` or;
     * `(AnyServerDrivenData, DeserializationContext)` or;
     * `(DeserializationContext, AnyServerDrivenData)` or;
     * `(DeserializationContext)`.
     */
    private fun validateDeserializers(
        functions: Sequence<KSFunctionDeclaration>,
    ): Sequence<KSFunctionDeclaration> {
        functions.forEach { fn ->
            val isNumberOfParamsCorrect = fn.parameters.size == 1 || fn.parameters.size == 2
            val typesAsStrings = fn.parameters.map { it.type.resolve().getQualifiedName() }
            val areTypesValid = typesAsStrings.find { !validDeserializerTypes.contains(it) } == null
            val areTypesUnique = typesAsStrings.distinct().size == typesAsStrings.size
            if (!isNumberOfParamsCorrect || !areTypesValid || !areTypesUnique)
                throw InvalidDeserializerParameters(fn)
        }
        return functions
    }

    private fun isComponent(fn: KSFunctionDeclaration): Boolean =
        fn.hasAnnotation(ClassNames.Composable)

    private fun isAction(fn: KSFunctionDeclaration): Boolean =
        // actions don't return anything while operations must return something
        fn.returnType?.resolve()?.isUnit() != false

    private fun createDeserializableFunctions(
        functions: Sequence<KSFunctionDeclaration>,
    ): List<DeserializableFunction> {
        val result = mutableListOf<DeserializableFunction>()
        functions.forEach {
            when {
                isComponent(it) -> result.add(DeserializableFunction(it, FunctionCategory.Component))
                isAction(it) -> result.add(DeserializableFunction(it, FunctionCategory.Action))
                else -> result.add(DeserializableFunction(it, FunctionCategory.Operation))
            }
        }
        return result
    }

    /**
     * Collects all functions annotated with `@AutoDeserialize` and classifies them in component,
     * action or operation.
     */
    internal fun collectDeserializableFunctions(resolver: Resolver): List<DeserializableFunction> {
        return createDeserializableFunctions(resolver.findAnnotations(AutoDeserialize::class))
    }

    /***
     * Collects all functions annotated with `@Deserializer`, i.e. the custom deserializers.
     */
    internal fun collectCustomDeserializers(resolver: Resolver): List<KSFunctionDeclaration> {
        return validateDeserializers(resolver.findAnnotations(Deserializer::class)).toList()
    }
}
