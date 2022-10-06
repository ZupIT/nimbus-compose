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

    private fun createNimbusComposable(builder: FileSpec.Builder, fn: KSFunctionDeclaration) {
        val component = FunctionInfo(fn)
        val fnBuilder = FunSpec.builder(component.name)
            .addAnnotation(ClassNames.Composable)
            .addParameter("data", ClassNames.ComponentData)
            .addStatement("var nimbus = NimbusTheme.nimbus")
            .addStatement("val properties = AnyServerDrivenData(data.node.properties)")
            .addStatement("properties.start()")

        fn.parameters.forEach {
            it.type.
        }

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

    fun createComponents(packageName: String, functions: List<KSFunctionDeclaration>) {
        val sourceFiles = functions.mapNotNull { it.containingFile }
        val componentsFile = FileSpec.builder(packageName,"generatedComponents")
            .addClassImport(ClassNames.NimbusTheme)
            .addClassImport(ClassNames.ComponentDeserializer)
            .addClassImport(ClassNames.NimbusMode)
            .addClassImport(ClassNames.Text)
            .addClassImport(ClassNames.Color)
            .addImport(PackageNames.composeRuntime, "remember")

        functions.forEach {
            createNimbusComposable(componentsFile, it)
        }

        val file = environment.codeGenerator.createNewFile(
            Dependencies(false, *sourceFiles.toList().toTypedArray()),
            packageName,
            "generatedComponents"
        )

        file.write(componentsFile.build().toString().toByteArray())
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val functions: Sequence<KSFunctionDeclaration> =
            resolver.findAnnotations(ServerDrivenComponent::class)
        if (!functions.iterator().hasNext()) return emptyList()

        val byPackage = functions.groupBy { it.packageName.asString() }

        byPackage.forEach {
            createComponents(it.key, it.value)
        }

        return (functions).filterNot { it.validate() }.toList()
    }
}