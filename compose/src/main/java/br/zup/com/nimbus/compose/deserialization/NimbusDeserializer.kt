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
import kotlin.reflect.jvm.jvmErasure

typealias DeserializationFunction<T> = (data: AnyServerDrivenData) -> T

object NimbusDeserializer {
    private val customDeserializers = mutableMapOf<KClass<*>, DeserializationFunction<*>>()

    private class ConstructorSelection<T>(
        val constructor: KFunction<T>,
        val arguments: Map<KParameter, Any?>,
    )

    private fun <T: Any>createInstance(
        clazz: KClass<T>,
        constructorSelection: ConstructorSelection<T>?,
        errors: MutableList<String>,
    ): T? {
        return constructorSelection?.let {
            try {
                it.constructor.callBy(constructorSelection.arguments)
            } catch(t: Throwable) {
                errors.add("Could not deserialize data into an instance of ${clazz.qualifiedName}" +
                        " because it doesn't respect the type of the arguments of the constructor")
                null
            }
        } ?: try {
            clazz.createInstance()
        } catch (t: Throwable) {
            errors.add("Could not find a suitable constructor for type ${clazz.qualifiedName}.")
            null
        }
    }

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

    private fun getValueForType(type: KType, data: AnyServerDrivenData, scope: Scope): Any? {
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
            typeClass.isSubclassOf(Enum::class) -> {
                @Suppress("UNCHECKED_CAST")
                val enumValues = typeClass.java.enumConstants as Array<Enum<*>>
                data.asEnum(enumValues, type.isMarkedNullable)
            }
            typeClass.isSubclassOf(List::class) -> getValueForList(type, data, scope)
            typeClass.isSubclassOf(Map::class) -> getValueForMap(type, data, scope)
            else -> deserializeUnknownType(data, typeClass, scope)
        }
    }

    private fun getName(element: KAnnotatedElement): String? = element.findAnnotation<Name>()?.name
    private fun getParamName(param: KParameter): String? = getName(param) ?: param.name
    private fun getPropertyName(prop: KProperty<*>): String = getName(prop) ?: prop.name

    private fun <T>selectConstructor(
        data: AnyServerDrivenData,
        constructors: Collection<KFunction<T>>,
        scope: Scope,
    ): ConstructorSelection<T>? {
        var bestArguments: Map<KParameter, Any?>? = null
        var bestConstructor: KFunction<T>? = null
        var bestCount = 0

        constructors.forEach { constructor ->
            val arguments = mutableMapOf<KParameter, Any?>()
            var count = 0
            for (param in constructor.parameters) {
                getParamName(param)?.let { name ->
                    when {
                        param.hasAnnotation<InjectScope>() -> {
                            if (scope::class.isSubclassOf(param.type.jvmErasure)) {
                                count++
                                arguments[param] = scope
                            } else if (!param.isOptional) return@forEach
                        }
                        data.containsKey(name) && !param.hasAnnotation<Ignore>() -> {
                            count++
                            val nextData = if (param.hasAnnotation<Root>()) data else data.get(name)
                            arguments[param] = getValueForType(param.type, nextData, scope)
                        }
                        param.isOptional -> {}
                        param.type.isMarkedNullable -> arguments[param] = null
                        // otherwise, this constructor is invalid
                        else -> return@forEach
                    }
                }
            }
            if (count > bestCount) {
                bestArguments = arguments
                bestConstructor = constructor
                bestCount = count
            }
        }

        return bestConstructor?.let {
            ConstructorSelection(
                constructor = it,
                arguments = bestArguments ?: emptyMap(),
            )
        }
    }

//    private fun <T: Any>buildFromConstructor(
//        data: AnyServerDrivenData,
//        scope: Scope,
//        constructor: KFunction<T>,
//    ): T? {
//
//    }
//
//    private fun <T: Any>buildFromClass(clazz: KClass<T>): T? {
//        return try {
//            clazz.createInstance()
//        } catch(_: Throwable) {
//            null
//        }
//    }
//
//    private fun <T: Any>construct(data: AnyServerDrivenData, scope: Scope, clazz: KClass<T>): T? {
//        val constructor = clazz.constructors.find { it.hasAnnotation<Deserializer>() }
//            ?: clazz.constructors.firstOrNull()
//        return if (constructor == null) {
//            buildFromClass(clazz)
//        } else {
//            buildFromConstructor(data, scope, constructor)
//        }
//    }

    private fun injectNimbusScopeIntoProperty(
        scope: Scope,
        prop: KMutableProperty<*>,
        instance: Any,
        errors: MutableList<String>,
    ) {
        if (scope::class.isSubclassOf(prop.returnType.jvmErasure)) {
            prop.setter.call(instance, scope)
        } else {
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
                        it.setter.call(instance, getValueForType(it.returnType, data, scope))
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
        val constructorSelection = selectConstructor(data, clazz.constructors, scope)
        val instance = createInstance(clazz, constructorSelection, data.errors)
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
