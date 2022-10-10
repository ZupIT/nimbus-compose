package br.zup.com.nimbus.compose.deserialization

import br.zup.com.nimbus.compose.deserialization.annotation.Ignore
import br.zup.com.nimbus.compose.deserialization.annotation.InjectScope
import br.zup.com.nimbus.compose.deserialization.annotation.Root
import com.zup.nimbus.core.scope.Scope

open class DeserializationError(message: String, cause: Throwable? = null):
    IllegalArgumentException(message, cause)

internal class AutoDeserializationError(
    val causes: List<String>,
    val property: DeserializableProperty,
    cause: Throwable?,
) : DeserializationError("", cause)

internal abstract class DeserializationErrorFactory {
    protected abstract fun ignore(error: AutoDeserializationError): String
    protected abstract fun injectScope(error: AutoDeserializationError, scope: Scope): String
    protected abstract fun rootProperty(error: AutoDeserializationError): String
    protected abstract fun commonProperty(error: AutoDeserializationError): String

    private fun makeError(message: String, error: AutoDeserializationError): DeserializationError {
        return DeserializationError(
            "$message\n  ${error.causes.joinToString("\n  ")}",
            error.cause,
        )
    }
            
    fun fromAutoDeserializationError(
        error: AutoDeserializationError, 
        scope: Scope,
    ): DeserializationError {
        return when (error.property.getAnnotation()) {
            is Ignore -> makeError(ignore(error), error)
            is InjectScope -> makeError(injectScope(error, scope), error)
            is Root -> makeError(rootProperty(error), error)
            else -> makeError(commonProperty(error), error)
        }
    }
}

internal object ConstructorErrorFactory: DeserializationErrorFactory() {
    override fun ignore(error: AutoDeserializationError): String = "Constructor " +
            "argument named \"${error.property}\" annotated with @Ignore must be either optional " +
            "or nullable."

    override fun injectScope(error: AutoDeserializationError, scope: Scope): String =
        "Constructor requires the scope to be injected, but the current scope is of a different " +
                "type than the one expected by the constructor. Expected: " +
                "${error.property.clazz.qualifiedName}, current: ${scope::class.qualifiedName}"

    override fun rootProperty(error: AutoDeserializationError): String = "Could not deserialize " +
            "type because the properties required to build the non-optional, non-nullable " +
            "property \"${error.property.name}\" are not available."

    override fun commonProperty(error: AutoDeserializationError): String = "Could not " +
            "deserialize type because non-optional, non-nullable property " +
            "\"${error.property.name}\" is not available."
}

internal object PropertyErrorFactory: DeserializationErrorFactory() {
    private fun generic(annotationType: String): String = "Error while applying @$annotationType " +
            "to member property. This is an error within the Server Driven Library. Please, report."

    override fun ignore(error: AutoDeserializationError): String = generic("Ignore")

    override fun injectScope(error: AutoDeserializationError, scope: Scope): String =
        generic("InjectScope")

    override fun rootProperty(error: AutoDeserializationError): String =
        generic("Root")

    override fun commonProperty(error: AutoDeserializationError): String = "Error while " +
            "deserializing member property. This is an error within the Server Driven Library. " +
            "Please, report."
}