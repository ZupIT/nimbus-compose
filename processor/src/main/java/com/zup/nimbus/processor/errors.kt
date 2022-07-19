package com.zup.nimbus.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter

open class NimbusCompilerException(message: String): Exception(message)

class NamelessParameterException(param: KSValueParameter, fn: KSFunctionDeclaration):
    NimbusCompilerException("\nError in function \"${fn.simpleName.asString()}\", parameter " +
            "\"${param.name?.asString() ?: ""}\"\n  at ${param.location}.\n  " +
            "Every parameter in the component function must be named.")

class DefaultParameterValueException(param: KSValueParameter, fn: KSFunctionDeclaration):
    NimbusCompilerException("\nError in function \"${fn.simpleName.asString()}\", parameter " +
            "\"${param.name?.asString() ?: ""}\"\n  at ${param.location}.\n  " +
            "Default values other than null are not supported.")

class NoConstructorException(clazz: KSClassDeclaration):
    NimbusCompilerException("\nError in class \"${clazz.simpleName}\"" +
            "\n  at: ${clazz.location}" +
            "\n  No default constructor found. To ignore an specific class or parameter " +
            "annotate it with @Ignore.")

class UnsupportedTypeException(param: String, type: String, category: String, fn: KSFunctionDeclaration):
    NimbusCompilerException("\nError in function \"${fn.simpleName.asString()}\", parameter " +
            "\"${param}\" of type $type and category $category" +
            "\n  at ${fn.location}" +
            "\n  This is a work in progress and we can't yet deserialize this yet. Please use a " +
            "custom component deserializer instead. You can also ignore specific classes or " +
            "parameters by annotating them with @Ignore.")