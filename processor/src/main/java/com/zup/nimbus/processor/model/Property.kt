package com.zup.nimbus.processor.model

import br.com.zup.nimbus.annotation.Root
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Location
import com.zup.nimbus.processor.ClassNames
import com.zup.nimbus.processor.error.InvalidUseOfRoot
import com.zup.nimbus.processor.utils.getQualifiedName
import com.zup.nimbus.processor.utils.hasAnnotation
import com.zup.nimbus.processor.utils.isKnown

/**
 * A more practical version of KSParameter given this project's needs.
 */
internal abstract class Property(
    val name: String,
    val type: KSType,
    val typeReference: KSTypeReference,
    val category: PropertyCategory,
    val location: Location,
    val parent: KSFunctionDeclaration,
) {
    companion object {
        private fun validateRoot(param: KSValueParameter, type: KSType) {
            // @Root can only be used for non-primitive classes
            if(type.isKnown()) throw InvalidUseOfRoot(param)
        }

        private fun validateNullability(param: KSValueParameter, name: String, type: KSType) {
            if(param.hasDefault && !type.isMarkedNullable) {
                println("Warning: getting default values via annotation processors is " +
                        "unsupported. Parameter with name $name at ${param.location} will be " +
                        "handled as required by the auto-deserialization.")
            }
        }

        fun nameFromParameter(param: KSValueParameter): String {
            return checkNotNull(param.name?.asString()) {
                "Every parameter must be named in Kotlin"
            }
        }

        fun typeFromParameter(param: KSValueParameter): KSType {
            val type = param.type.resolve()
            validateNullability(param, nameFromParameter(param), type)
            return type
        }

        fun categoryFromParam(param: KSValueParameter, type: KSType): PropertyCategory {
            return when {
                param.hasAnnotation<Root>() -> {
                    validateRoot(param, type)
                    PropertyCategory.Root
                }
                param.type.resolve().getQualifiedName()
                        == ClassNames.DeserializationContext.canonicalName -> PropertyCategory.Context
                param.type.hasAnnotation(ClassNames.Composable) -> PropertyCategory.Composable
                else -> PropertyCategory.Common
            }
        }
    }

    /**
     * Creates the code for accessing this property in the AnyServerDrivenData referred by ref.
     */
    abstract fun getAccessString(ref: String): String

    /**
     * Creates the code for verifying if this property exists the AnyServerDrivenData referred by
     * ref.
     */
    abstract fun getContainsString(ref: String): String
}
