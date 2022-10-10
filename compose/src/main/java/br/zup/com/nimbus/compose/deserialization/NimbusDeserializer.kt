package br.zup.com.nimbus.compose.deserialization

import br.zup.com.nimbus.compose.deserialization.annotation.Deserializer
import br.zup.com.nimbus.compose.deserialization.annotation.Ignore
import br.zup.com.nimbus.compose.deserialization.annotation.Name
import br.zup.com.nimbus.compose.deserialization.annotation.Root
import br.zup.com.nimbus.compose.deserialization.annotation.InjectScope
import com.zup.nimbus.core.deserialization.AnyServerDrivenData
import com.zup.nimbus.core.scope.Scope
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure

typealias DeserializationFunction<T> = (data: AnyServerDrivenData) -> T

open class DeserializableProperty(
    val name: String,
    val annotations: List<Annotation>,
    val optional: Boolean,
    val nullable: Boolean,
    val clazz: KClass<*>,
    val arguments: List<KTypeProjection>,
) {
    inline fun <reified T: Annotation> findAnnotation(): T? = annotations.find { it is T } as? T
    inline fun <reified T: Annotation> hasAnnotation(): Boolean = findAnnotation<T>() != null
    fun getAnnotation() = annotations.find {
        it is Ignore || it is InjectScope || it is Root
    }
}

internal class DeserializableMutableProperty(val property: KMutableProperty<*>):
    DeserializableProperty(
        name = property.name,
        annotations = property.annotations,
        optional = true,
        nullable = property.returnType.isMarkedNullable,
        clazz = property.returnType.jvmErasure,
        arguments = property.returnType.arguments,
    )

internal class DeserializableParameter(val parameter: KParameter): DeserializableProperty(
    name = parameter.name ?: "_:unnamed_constructor_parameter",
    annotations = parameter.annotations,
    optional = parameter.isOptional,
    nullable = parameter.type.isMarkedNullable,
    clazz = parameter.type.jvmErasure,
    arguments = parameter.type.arguments,
)

object NimbusDeserializer {
    private val customDeserializers = mutableMapOf<KClass<*>, DeserializationFunction<*>>()

    private fun getValueForList(
        arguments: List<KTypeProjection>,
        nullable: Boolean,
        data: AnyServerDrivenData,
        scope: Scope,
    ): Any? {
        val genericType = arguments.firstOrNull()?.type
            ?: throw DeserializationError("Cannot deserialize List without a generic type.")
        return data.asList(nullable)?.map {
            getValueForType(
                nullable = genericType.isMarkedNullable,
                clazz = genericType.jvmErasure,
                arguments = genericType.arguments,
                data = it,
                scope = scope,
            )
        }
    }

    private fun getValueForMap(
        arguments: List<KTypeProjection>,
        nullable: Boolean,
        data: AnyServerDrivenData,
        scope: Scope,
    ): Any? {
        val keyType = arguments.getOrNull(0)?.type
        val valueType = arguments.getOrNull(1)?.type ?: throw DeserializationError(
            "Cannot deserialize Map where values have no generic type."
        )
        if (keyType?.jvmErasure != String::class) {
            throw DeserializationError("Cannot deserialize Map with non-string keys.")
        }

        return data.asMap(nullable)?.mapValues {
            getValueForType(
                nullable = valueType.isMarkedNullable,
                clazz = valueType.jvmErasure,
                arguments = valueType.arguments,
                data = it.value,
                scope = scope,
            )
        }
    }

    private fun getValueForType(
        nullable: Boolean,
        clazz: KClass<*>,
        arguments: List<KTypeProjection>,
        data: AnyServerDrivenData,
        scope: Scope,
    ): Any? {
        return when {
            data.isNull() -> null
            customDeserializers.containsKey(clazz) -> {
                customDeserializers[clazz]?.let { it(data) }
            }
            clazz == String::class -> data.asString(nullable)
            clazz == Boolean::class -> data.asBoolean(nullable)
            clazz == Int::class -> data.asInt(nullable)
            clazz == Long::class -> data.asLong(nullable)
            clazz == Double::class -> data.asDouble(nullable)
            clazz == Float::class -> data.asFloat(nullable)
            clazz == Function0::class -> {
                // todo: verify if return type is Unit
                val event = data.asEvent(nullable)
                event?.let { ev ->
                    object : Function0<Unit> {
                        override fun invoke() = ev.run()
                    }
                }
            }
            clazz == Function1::class -> {
                // todo: verify if return type is Unit
                val event = data.asEvent(nullable)
                event?.let { ev -> { value: Any -> ev.run(value) } }
            }
            clazz == List::class -> getValueForList(arguments, nullable, data, scope)
            clazz == Map::class -> getValueForMap(arguments, nullable, data, scope)
            clazz.isSubclassOf(Enum::class) -> {
                @Suppress("UNCHECKED_CAST")
                val enumValues = clazz.java.enumConstants as Array<Enum<*>>
                data.asEnum(enumValues, nullable)
            }
            else -> deserializeUnknownType(data, clazz, scope)
        }
    }

    private fun checkNullability(
        property: DeserializableProperty,
        data: AnyServerDrivenData,
        causeOfError: DeserializationError?,
        setValue: (Any?) -> Unit,
    ) {
        val errors = data.collectErrors()
        if (!property.optional) {
            if (!property.nullable) throw AutoDeserializationError(errors, property, causeOfError)
            setValue(null)
        }
    }

    private fun injectScope(
        scope: Scope,
        property: DeserializableProperty,
        setValue: (Any?) -> Unit,
    ) {
        if (scope::class.isSubclassOf(property.clazz)) {
            setValue(scope)
        }
    }

    private fun handleRootProperty(
        property: DeserializableProperty,
        data: AnyServerDrivenData,
        scope: Scope,
        setValue: (Any?) -> Unit,
    ) {
        setValue(
            getValueForType(
                nullable = property.nullable,
                clazz = property.clazz,
                arguments = property.arguments,
                data = data,
                scope = scope,
            )
        )
    }

    private fun handleCommonProperty(
        property: DeserializableProperty,
        data: AnyServerDrivenData,
        scope: Scope,
        setValue: (Any?) -> Unit,
    ) {
        val name = property.findAnnotation<Name>()?.name ?: property.name
        if (data.containsKey(name)) {
            setValue(getValueForType(
                nullable = property.nullable,
                clazz = property.clazz,
                arguments = property.arguments,
                data = data.get(name),
                scope = scope,
            ))
        }
    }

    private fun <T, U: DeserializableProperty>deserializeProperties(
        properties: List<U>,
        data: AnyServerDrivenData,
        scope: Scope,
        keyFactory: (U) -> T,
    ): Map<T, Any?> {
        val result = mutableMapOf<T, Any?>()
        properties.forEach { property ->
            val annotation = property.getAnnotation()
            val key = keyFactory(property)
            val setValue = { value: Any? -> result[key] = value }
            var causeOfError: DeserializationError? = null
            try {
                when (annotation) {
                    is Ignore -> {}
                    is InjectScope -> injectScope(scope, property, setValue)
                    is Root -> handleRootProperty(property, data, scope, setValue)
                    else -> handleCommonProperty(property, data, scope, setValue)
                }
            } catch (e: DeserializationError) {
                causeOfError = e
                result[key] = null
            }
            if (result[key] == null) checkNullability(property, data, causeOfError, setValue)

        }
        return result
    }

    fun deserializeProperties(
        properties: List<DeserializableProperty>,
        data: AnyServerDrivenData,
        scope: Scope,
    ): Map<String, Any?> = deserializeProperties(properties, data, scope) { it.name }

    private fun <T: Any>buildFromConstructor(
        data: AnyServerDrivenData,
        scope: Scope,
        constructor: KFunction<T>,
    ): T {
        val properties = constructor.parameters.map { DeserializableParameter(it) }
        try {
            val arguments = deserializeProperties(properties, data, scope) { it.parameter }
            return constructor.callBy(arguments)
        } catch(error: AutoDeserializationError) {
            throw ConstructorErrorFactory.fromAutoDeserializationError(error, scope)
        } catch(e: DeserializationError) {
            throw e
        } catch(t: Throwable) {
            throw DeserializationError("Could not build instance of class: ${t.message}")
        }
    }

    private fun <T: Any>findConstructor(clazz: KClass<T>): KFunction<T> {
        return clazz.constructors.find { it.hasAnnotation<Deserializer>() }
            ?: clazz.constructors.firstOrNull() ?: throw DeserializationError(
                "Cannot deserialize type ${clazz.qualifiedName} because it doesn't have a public " +
                        "constructor."
            )
    }

    private fun populateInstanceWithData(
        instance: Any,
        clazz: KClass<*>,
        data: AnyServerDrivenData,
        scope: Scope,
    ) {
        val properties = clazz.declaredMemberProperties.filterIsInstance<KMutableProperty<*>>()
            .map { DeserializableMutableProperty(it) }
        try {
            val deserialized = deserializeProperties(properties, data, scope) { it.property }
            deserialized.forEach {
                try {
                    it.key.setter.call(instance, it.value)
                } catch (t: Throwable) {
                    throw DeserializationError(
                        "Error while deserializing ${it.key.name} in ${clazz.qualifiedName}:\n  " +
                                "${t.message}"
                    )
                }
            }
        } catch (e: AutoDeserializationError) {
            throw PropertyErrorFactory.fromAutoDeserializationError(e, scope)
        }
    }

    private fun <T: Any>deserializeUnknownType(
        data: AnyServerDrivenData,
        clazz: KClass<T>,
        scope: Scope,
    ): T {
        try {
            val constructor = findConstructor(clazz)
            val instance = buildFromConstructor(data, scope, constructor)
            populateInstanceWithData(instance, clazz, data, scope)
            return instance
        } catch (t: Throwable) {
            throw DeserializationError(
                "Could not deserialize class ${clazz.qualifiedName} with the given data.",
                t,
            )
        }
    }

    fun <T: Any>addDeserializer(clazz: KClass<T>, deserializer: DeserializationFunction<T>) {
        customDeserializers[clazz] = deserializer
    }

    fun deserialize(data: AnyServerDrivenData, scope: Scope, type: KType): Any? =
        getValueForType(
            nullable = type.isMarkedNullable,
            clazz = type.jvmErasure,
            arguments = type.arguments,
            data = data,
            scope = scope,
        )

    fun <T: Any>deserialize(data: AnyServerDrivenData, scope: Scope, clazz: KClass<T>): T? {
        val result = getValueForType(
            nullable = true,
            clazz = clazz,
            arguments = emptyList(),
            data = data,
            scope = scope,
        )
        @Suppress("UNCHECKED_CAST")
        return if (result != null && result::class.isSubclassOf(clazz)) result as T else null
    }
}
