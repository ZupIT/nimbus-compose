package com.zup.nimbus.processor

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import kotlin.reflect.KClass

class ServerDrivenProcessor(private val environment: SymbolProcessorEnvironment): SymbolProcessor {
    private fun Resolver.findAnnotations(kClass: KClass<*>) =
        getSymbolsWithAnnotation(kClass.qualifiedName.toString())
            .filterIsInstance<KSFunctionDeclaration>()

    private fun handleDeserializer(param: ParameterInfo, fnBuilder: FunSpec.Builder) {
        if (param.isRoot) {
            fnBuilder.addStatement(
                "val %L = %L.deserialize(properties, data, %S)",
                param.name,
                param.deserializer!!.simpleName,
                param.name,
            )
        } else if (param.nullable) {
            fnBuilder.addCode("""
                        |val %L = if (properties.enter(%S, true)) {
                        |    val result = %L.deserialize(properties, data, %S)
                        |    properties.leave()
                        |    result
                        |} else null
                        |""".trimMargin(),
                param.name, param.name, param.deserializer!!.simpleName, param.name
            )
        } else {
            fnBuilder.addStatement("properties.enter(%S, false)", param.name)
            fnBuilder.addStatement(
                "val %L = %L.deserialize(properties, data, %S)",
                param.name,
                param.deserializer!!.simpleName,
                param.name,
            )
            fnBuilder.addStatement("properties.leave()", param.name)
        }
    }

    private fun handleServerDrivenAction(param: ParameterInfo, fnBuilder: FunSpec.Builder) {
        fnBuilder.addStatement(
            "val %LEvent = properties.asEvent%L(%S)",
            param.name,
            if (param.nullable) "OrNull" else "",
            param.name,
        )
        val template = if (param.nullable) "val %L: (%L)? = %LEvent?.let { event -> { event.run(%L) } }"
        else "val %L: %L = { %LEvent.run(%L) }"
        fnBuilder.addStatement(
            template,
            param.name,
            if (param.arity == 0) "() -> Unit" else "(Any) -> Unit",
            param.name,
            if (param.arity == 0) "" else "it",
        )
    }

    private fun createNimbusComposable(
        builder: FileSpec.Builder,
        fn: KSFunctionDeclaration,
    ): Set<KSClassDeclaration> {
        val mustDeserialize = mutableSetOf<KSClassDeclaration>()
        val component = FunctionInfo(fn)
        val fnBuilder = FunSpec.builder(component.name)
            .addAnnotation(ClassNames.Composable)
            .addParameter("data", ClassNames.ComponentData)
            .addStatement("var nimbus = NimbusTheme.nimbus")
            .addStatement("val properties = remember { ComponentDeserializer(logger = nimbus.logger, node = data.node) }")
            .addStatement("properties.start()")

        component.parameters.forEach {
            if (it.deserializer != null) {
                if (it.deserializer.packageName != fn.packageName.asString()) {
                    builder.addClassImport(it.deserializer)
                }
                handleDeserializer(it, fnBuilder)
            }
            else {
                when (it.category) {
                    TypeCategory.Primitive -> fnBuilder.addStatement(
                        "val %L = properties.as%L%L(%S)",
                        it.name,
                        it.type,
                        if (it.nullable) "OrNull" else "",
                        it.name,
                    )
                    TypeCategory.Enum -> {
                        builder.addImport(it.packageName, it.type)
                        fnBuilder.addStatement(
                            "val %L = properties.asEnum%L(%S, %L.values())",
                            it.name,
                            if (it.nullable) "OrNull" else "",
                            it.name,
                            it.type,
                        )
                    }
                    TypeCategory.ServerDrivenAction -> handleServerDrivenAction(it, fnBuilder)
                    TypeCategory.Composable -> fnBuilder.addStatement(
                        "val %L = data.children",
                        it.name,
                    )
                    TypeCategory.Deserializable -> {
                        mustDeserialize.addAll(it.mustDeserialize)
                        builder.addImport(it.packageName, it.type)
                        fnBuilder.addStatement(
                            "val %L = NimbusEntityDeserializer.deserialize(properties, data, %L::class)",
                            it.name,
                            it.type,
                        )
                    }
                    else -> {
                        throw UnsupportedTypeException(it.name, it.type, it.category.name, fn)
                    }
                }
            }
        }

        fnBuilder.addStatement("val isSuccessful = properties.end()")
            .addStatement(
                "if (isSuccessful) { %L(%L) }",
                component.name,
                component.parameters.joinToString(", ") { "${it.name} = ${it.name}" }
            )
            .addStatement(
                "else if (nimbus.mode == NimbusMode.Development) { Text(%P, color = Color.Red) }",
                "Error while deserializing \${data.node.component}."
            )

        builder.addFunction(fnBuilder.build())
        return mustDeserialize
    }

    fun createComponents(
        packageName: String,
        functions: List<KSFunctionDeclaration>,
        entityDeserializerRef: ClassName,
    ): Set<KSClassDeclaration> {
        val mustDeserialize = mutableSetOf<KSClassDeclaration>()
        val sourceFiles = functions.mapNotNull { it.containingFile }
        val componentsFile = FileSpec.builder(packageName,"generatedComponents")
            .addClassImport(ClassNames.NimbusTheme)
            .addClassImport(ClassNames.ComponentDeserializer)
            .addClassImport(ClassNames.NimbusMode)
            .addClassImport(ClassNames.Text)
            .addClassImport(ClassNames.Color)
            .addImport(PackageNames.composeRuntime, "remember")

        functions.forEach {
            mustDeserialize.addAll(createNimbusComposable(componentsFile, it))
        }

        if (mustDeserialize.isNotEmpty()) componentsFile.addClassImport(entityDeserializerRef)

        val file = environment.codeGenerator.createNewFile(
            Dependencies(false, *sourceFiles.toList().toTypedArray()),
            packageName,
            "generatedComponents"
        )

        file.write(componentsFile.build().toString().toByteArray())
        return mustDeserialize
    }

    fun createClassDeserializer(builder: FileSpec.Builder, clazz: KSClassDeclaration): FunSpec {
        val name = "${clazz.packageName.asString()}.${clazz.simpleName.asString()}"
            .replace(".", "_")
        val fnBuilder = FunSpec.builder(name)
            .addParameter("properties", ClassNames.ComponentDeserializer)
            .addParameter("data", ClassNames.ComponentData)
            .addModifiers(KModifier.PRIVATE)
            .returns(ClassName(clazz.packageName.asString(), clazz.simpleName.asString()))
        val constructorInfo = FunctionInfo(
            clazz.primaryConstructor ?: throw NoConstructorException(clazz)
        )
        constructorInfo.parameters.forEach {
            if (it.deserializer != null) {
                if (it.deserializer.packageName != clazz.packageName.asString()) {
                    builder.addClassImport(it.deserializer)
                }
                handleDeserializer(it, fnBuilder)
            }
            else {
                when (it.category) {
                    TypeCategory.Primitive -> fnBuilder.addStatement(
                        "val %L = properties.as%L%L(%S)",
                        it.name,
                        it.type,
                        if (it.nullable) "OrNull" else "",
                        it.name,
                    )
                    TypeCategory.Enum -> {
                        builder.addImport(it.packageName, it.type)
                        fnBuilder.addStatement(
                            "val %L = properties.asEnum%L(%S, %L.values())",
                            it.name,
                            if (it.nullable) "OrNull" else "",
                            it.name,
                            it.type,
                        )
                    }
                    TypeCategory.ServerDrivenAction -> handleServerDrivenAction(it, fnBuilder)
                    else -> {
                        throw UnsupportedTypeException(
                            it.name,
                            it.type,
                            it.category.name,
                            clazz.primaryConstructor!!
                        )
                    }
                }
            }
        }

        fnBuilder.addStatement(
            "return %L(%L)",
            clazz.simpleName.asString(),
            constructorInfo.parameters.joinToString(", ") { "${it.name} = ${it.name}" }
        )

        return fnBuilder.build()
    }

    fun createEntityDeserializer(
        mustDeserialize: Set<KSClassDeclaration>,
        entityDeserializerRef: ClassName,
    ) {
        val poetFile = FileSpec.builder(
            entityDeserializerRef.packageName,
            entityDeserializerRef.simpleName
        )

        val objectBuilder = TypeSpec.objectBuilder("NimbusEntityDeserializer")
            .addProperty(
                PropertySpec.builder(
                    "deserializers",
                    ClassName("kotlin.collections", "MutableMap")
                        .parameterizedBy(
                            String::class.asTypeName(),
                            LambdaTypeName.get(
                                parameters = listOf(
                                    ParameterSpec.builder(
                                        "properties",
                                        ClassNames.ComponentDeserializer,
                                    ).build(),
                                    ParameterSpec.builder(
                                        "data",
                                        ClassNames.ComponentData,
                                    ).build()
                                ),
                                returnType = Any::class.asTypeName(),
                            )
                        ),
                    KModifier.PRIVATE,
                )
                .initializer(
                    CodeBlock.of(
                        "mutableMapOf(%L)",
                        mustDeserialize.joinToString(", ") {
                            val name = "${it.packageName.asString()}.${it.simpleName.asString()}"
                            "\"$name\" to { properties, data -> ${name.replace(".", "_")}(properties, data) }"
                        },
                    )
                )
                .build()
            )
            .addFunction(
                FunSpec.builder("deserialize")
                    .addTypeVariables(
                        listOf(
                            TypeVariableName("T"),
                            TypeVariableName(
                                "U",
                                listOf(
                                    KClass::class.asClassName()
                                        .parameterizedBy(TypeVariableName("T"))
                                )
                            )
                        )
                    )
                    .addParameter("properties", ClassNames.ComponentDeserializer)
                    .addParameter("data", ClassNames.ComponentData)
                    .addParameter("clazz", TypeVariableName("U"))
                    .returns(TypeVariableName("T"))
                    .addStatement(
                        "return deserializers.get(clazz.qualifiedName ?: \"\")?.let " +
                                "{ it(properties, data) as T } ?: throw IllegalArgumentException(%P)",
                        "\${clazz.simpleName} is an invalid Server Driven entity because no " +
                                "deserializer has been found for it."
                    )
                    .build()
            )

        mustDeserialize.forEach { objectBuilder.addFunction(createClassDeserializer(poetFile, it)) }

        poetFile.addType(objectBuilder.build())

        val file = environment.codeGenerator.createNewFile(
            Dependencies(
                false,
                *mustDeserialize.mapNotNull { it.containingFile }.toList().toTypedArray()
            ),
            entityDeserializerRef.packageName,
            entityDeserializerRef.simpleName
        )

        file.write(poetFile.build().toString().toByteArray())
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val functions: Sequence<KSFunctionDeclaration> =
            resolver.findAnnotations(ServerDrivenComponent::class)
        if (!functions.iterator().hasNext()) return emptyList()

        val mustDeserialize = mutableSetOf<KSClassDeclaration>()
        val byPackage = functions.groupBy { it.packageName.asString() }
        val entityDeserializerRef = ClassName(
            functions.first().packageName.asString(),
            "NimbusEntityDeserializer",
        )

        byPackage.forEach {
            mustDeserialize.addAll(createComponents(it.key, it.value, entityDeserializerRef))
        }

        if (mustDeserialize.isNotEmpty()) {
            createEntityDeserializer(mustDeserialize, entityDeserializerRef)
        }

        return (functions).filterNot { it.validate() }.toList()
    }
}