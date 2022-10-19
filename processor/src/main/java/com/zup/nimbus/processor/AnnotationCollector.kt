package com.zup.nimbus.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.zup.nimbus.processor.annotation.AutoDeserialize
import com.zup.nimbus.processor.annotation.Deserializer
import com.zup.nimbus.processor.error.InvalidDeserializerParameters
import com.zup.nimbus.processor.model.DeserializableFunction
import com.zup.nimbus.processor.model.DeserializationType
import com.zup.nimbus.processor.model.FunctionCategory
import com.zup.nimbus.processor.utils.findAnnotations
import com.zup.nimbus.processor.utils.getAnnotation
import com.zup.nimbus.processor.utils.getQualifiedName
import com.zup.nimbus.processor.utils.hasAnnotation

object AnnotationCollector {
    private fun validateDeserializers(
        functions: Sequence<KSFunctionDeclaration>,
    ): Sequence<KSFunctionDeclaration> {
        functions.forEach {
            val isNumberOfParamsCorrect = it.parameters.size == 1 || it.parameters.size == 2
            val isFirstParamCorrect = it.parameters.firstOrNull()?.type?.resolve()
                ?.getQualifiedName() == ClassNames.AnyServerDrivenData.canonicalName
            val isSecondParamCorrect = it.parameters.size == 1 ||
                    it.parameters.getOrNull(1)?.type?.resolve()?.getQualifiedName() ==
                    ClassNames.DeserializationContext.canonicalName
            if (!isNumberOfParamsCorrect || !isFirstParamCorrect || !isSecondParamCorrect)
                throw InvalidDeserializerParameters(it)
        }
        return functions
    }

    private fun validateComponent(fn: KSFunctionDeclaration): DeserializableFunction {
        // todo: validate
        return DeserializableFunction(fn, FunctionCategory.Component)
    }

    private fun validateAction(fn: KSFunctionDeclaration): DeserializableFunction {
        // todo: validate
        return DeserializableFunction(fn, FunctionCategory.Action)
    }

    private fun validateOperation(fn: KSFunctionDeclaration): DeserializableFunction {
        // todo: validate
        return DeserializableFunction(fn, FunctionCategory.Operation)
    }

    private fun createDeserializableFunctions(
        functions: Sequence<KSFunctionDeclaration>,
    ): List<DeserializableFunction> {
        val result = mutableListOf<DeserializableFunction>()
        functions.forEach {
            when(it.getAnnotation<AutoDeserialize>()?.type) {
                DeserializationType.Component -> result.add(validateComponent(it))
                DeserializationType.Action -> result.add(validateAction(it))
                DeserializationType.Operation -> result.add(validateOperation(it))
                else -> {}
            }
        }
        return result
    }

    fun collectDeserializableFunctions(resolver: Resolver): List<DeserializableFunction> {
        return createDeserializableFunctions(resolver.findAnnotations(AutoDeserialize::class))
    }

    fun collectCustomDeserializers(resolver: Resolver): List<KSFunctionDeclaration> {
        return validateDeserializers(resolver.findAnnotations(Deserializer::class)).toList()
    }
}
