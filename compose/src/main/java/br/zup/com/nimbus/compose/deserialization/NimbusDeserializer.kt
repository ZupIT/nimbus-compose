package br.zup.com.nimbus.compose.deserialization

import br.zup.com.nimbus.compose.deserialization.annotation.Deserializer
import br.zup.com.nimbus.compose.deserialization.annotation.Ignore
import br.zup.com.nimbus.compose.deserialization.annotation.Name
import br.zup.com.nimbus.compose.deserialization.annotation.Root
import br.zup.com.nimbus.compose.deserialization.annotation.InjectScope
import com.zup.nimbus.core.deserialization.AnyServerDrivenData
import com.zup.nimbus.core.scope.Scope
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.jvm.jvmErasure

typealias DeserializationFunction<T> = (data: AnyServerDrivenData) -> T

object NimbusDeserializer {
    private val customDeserializers = mutableMapOf<KClass<*>, DeserializationFunction<*>>()

    private fun getValueForList(type: KType, data: AnyServerDrivenData, scope: Scope): Any? {
       return type.arguments.firstOrNull()?.type?.let { genericType ->
           data.asList(type.isMarkedNullable)?.map {
               getValueForType(genericType, it, scope)
           }
       } ?: run {
           data.errors.add("Cannot deserialize List without a generic type.")
           null
       }
    }

    private fun getValueForMap(type: KType, data: AnyServerDrivenData, scope: Scope): Any? {
        val keyType = type.arguments.getOrNull(0)?.type
        val valueType = type.arguments.getOrNull(1)?.type
        if (keyType?.jvmErasure != String::class) {
            data.errors.add("Cannot deserialize Map with non-string keys.")
            return null
        }

        return valueType?.let { genericType ->
            data.asMap(type.isMarkedNullable)?.mapValues {
                getValueForType(genericType, it.value, scope)
            }
        } ?: run {
            data.errors.add("Cannot deserialize Map where values have no generic type.")
            null
        }
    }

    private fun getValueForType(
        type: KType,
        data: AnyServerDrivenData,
        scope: Scope,
    ): Any? {
        val typeClass = type.jvmErasure
        return when {
            data.isNull() -> {
                if (!type.isMarkedNullable) {
                    data.errors.add("Unexpected null value at \"${data.path}\".")
                }
                null
            }
            customDeserializers.containsKey(typeClass) -> {
                customDeserializers[typeClass]?.let { it(data) }
            }
            typeClass == String::class -> data.asString(type.isMarkedNullable)
            typeClass == Boolean::class -> data.asBoolean(type.isMarkedNullable)
            typeClass == Int::class -> data.asInt(type.isMarkedNullable)
            typeClass == Long::class -> data.asLong(type.isMarkedNullable)
            typeClass == Double::class -> data.asDouble(type.isMarkedNullable)
            typeClass == Float::class -> data.asFloat(type.isMarkedNullable)
            typeClass == Function0::class -> {
                // todo: verify if return type is Unit
                val event = data.asEvent(type.isMarkedNullable)
                event?.let { ev ->
                    object : Function0<Unit> {
                        override fun invoke() = ev.run()
                    }
                }
            }
            typeClass == Function1::class -> {
                // todo: verify if return type is Unit
                val event = data.asEvent(type.isMarkedNullable)
                event?.let { ev -> { value: Any -> ev.run(value) } }
            }
            typeClass == List::class -> getValueForList(type, data, scope)
            typeClass == Map::class -> getValueForMap(type, data, scope)
            typeClass.isSubclassOf(Enum::class) -> {
                @Suppress("UNCHECKED_CAST")
                val enumValues = typeClass.java.enumConstants as Array<Enum<*>>
                data.asEnum(enumValues, type.isMarkedNullable)
            }
            else -> deserializeUnknownType(data, typeClass, scope)
        }
    }

    private fun getName(element: KAnnotatedElement): String? = element.findAnnotation<Name>()?.name
    private fun getParamName(param: KParameter): String? = getName(param) ?: param.name
    private fun getPropertyName(prop: KProperty<*>): String = getName(prop) ?: prop.name

//    fun deserializeProperties(
//        properties: List<DeserializableProperty>,
//    ): List<DeserializedProperty>) {
//
//    }

    private fun <T: Any>buildFromConstructor(
        data: AnyServerDrivenData,
        scope: Scope,
        constructor: KFunction<T>,
    ): T? {
        val arguments = mutableMapOf<KParameter, Any?>()
        for (parameter in constructor.parameters) {
            when {
                parameter.hasAnnotation<Ignore>() -> {
                    if (!parameter.isOptional) {
                        if (parameter.type.isMarkedNullable) arguments[parameter] = null
                        else {
                            data.errors.add("Constructor argument annotated with @Ignore must be " +
                                    "either optional or nullable.")
                            return null
                        }
                    }
                }
                parameter.hasAnnotation<InjectScope>() -> {
                    if (scope::class.isSubclassOf(parameter.type.jvmErasure)) {
                        arguments[parameter] = scope
                    } else if (!parameter.isOptional) {
                        if (parameter.type.isMarkedNullable) {
                            arguments[parameter] = null
                        } else {
                            data.errors.add("Constructor requires the scope to be " +
                                    "injected, but the current scope is of a different type than the" +
                                    "one expected by the constructor. Expected: " +
                                    "${parameter.type.jvmErasure.qualifiedName}, " +
                                    "current: ${scope::class.qualifiedName}"
                            )
                            return null
                        }
                    }
                }
                parameter.hasAnnotation<Root>() -> {
                    // This is not a great solution, but it should work. We must rethink error
                    // handling in deserialization. This will suppress more errors than the
                    // ideal. It's also very bad coding.
                    val errorsBeforeDeserialization = data.errors.size
                    arguments[parameter] = getValueForType(parameter.type, data, scope)
                    if (!parameter.isOptional && arguments[parameter] == null) {
                        if (parameter.type.isMarkedNullable) arguments[parameter] = null
                        else {
                            data.errors.add("Could not deserialize type because the properties " +
                                    "required to build the non-optional, non-nullable property " +
                                    "\"${parameter.name ?: "unnamed"}\" are not available.")
                            return null
                        }
                    }
                    while (data.errors.size > errorsBeforeDeserialization) data.errors.removeLast()
                }
                else -> {
                    val name = getParamName(parameter)
                    if (name != null && data.containsKey(name)) {
                        arguments[parameter] = getValueForType(parameter.type, data.get(name), scope)
                    } else if (!parameter.isOptional) {
                        if (parameter.type.isMarkedNullable) arguments[parameter] = null
                        else {
                            data.errors.add("Could not deserialize type because non-optional, " +
                                    "non-nullable property \"${parameter.name ?: "unnamed"}\" is" +
                                    "not available.")
                            return null
                        }
                    }
                }
            }
        }
        return try {
            constructor.callBy(arguments)
        } catch(t: Throwable) {
            data.errors.add("Could not build instance of class: ${t.message}")
            null
        }
    }

    private fun <T: Any>buildFromClass(clazz: KClass<T>, errors: MutableList<String>): T? {
        return try {
            clazz.createInstance()
        } catch(_: Throwable) {
            errors.add("Cannot deserialize type ${clazz.qualifiedName} because it doesn't have a " +
                    "public constructor.")
            null
        }
    }

    private fun <T: Any>createInstance(
        data: AnyServerDrivenData,
        clazz: KClass<T>,
        scope: Scope,
    ): T? {
        val constructor = clazz.constructors.find { it.hasAnnotation<Deserializer>() }
            ?: clazz.constructors.firstOrNull()
        return if (constructor == null) {
            buildFromClass(clazz, data.errors)
        } else {
            buildFromConstructor(data, scope, constructor)
        }
    }

    private fun injectNimbusScopeIntoProperty(
        scope: Scope,
        prop: KMutableProperty<*>,
        instance: Any,
        errors: MutableList<String>,
    ) {
        if (scope::class.isSubclassOf(prop.returnType.jvmErasure)) {
            prop.setter.call(instance, scope)
        } else if (!prop.returnType.isMarkedNullable) {
            errors.add("Can't inject current Nimbus scope because the expected type " +
                    "(${prop.returnType.jvmErasure.qualifiedName}) is different than the current " +
                    "scope type (${scope::class.qualifiedName})")
        }
    }

    private fun populateInstanceWithData(
        instance: Any,
        clazz: KClass<*>,
        data: AnyServerDrivenData,
        scope: Scope,
    ) {
        clazz.declaredMemberProperties.forEach {
            if (it is KMutableProperty<*> && !it.hasAnnotation<Ignore>()) {
                try {
                    if (it.hasAnnotation<InjectScope>()) {
                        injectNimbusScopeIntoProperty(scope, it, instance, data.errors)
                    } else if (it.hasAnnotation<Root>()) {
                        // This is not a great solution, but it should work. We must rethink error
                        // handling in deserialization. This will suppress more errors than the
                        // ideal. It's also very bad coding.
                        val errorsBeforeDeserialization = data.errors.size
                        val value = getValueForType(it.returnType, data, scope)
                        if (value != null) {
                            it.setter.call(instance, value)
                            while (data.errors.size > errorsBeforeDeserialization)
                                data.errors.removeLast()
                        }
                    } else if (data.containsKey(it.name)) {
                        it.setter.call(
                            instance,
                            getValueForType(it.returnType, data.get(getPropertyName(it)), scope),
                        )
                    }
                } catch(t: Throwable) {
                    data.errors.add("could not set value for ${it.name}: " +
                            (t.message ?: "unknown error")
                    )
                }
            }
        }
    }

    private fun <T: Any>deserializeUnknownType(
        data: AnyServerDrivenData,
        clazz: KClass<T>,
        scope: Scope,
    ): T? {
        val instance = createInstance(data, clazz, scope)
        instance?.let { populateInstanceWithData(it, clazz, data, scope) }
        return instance
    }

    fun <T: Any>addDeserializer(clazz: KClass<T>, deserializer: DeserializationFunction<T>) {
        customDeserializers[clazz] = deserializer
    }

    fun deserialize(data: AnyServerDrivenData, scope: Scope, type: KType): Any? =
        getValueForType(type, data, scope)

    fun <T: Any>deserialize(data: AnyServerDrivenData, scope: Scope, clazz: KClass<T>): T? {
        val result = getValueForType(clazz.createType(), data, scope)
        @Suppress("UNCHECKED_CAST")
        return if (result != null && result::class.isSubclassOf(clazz)) result as T else null
    }
}
