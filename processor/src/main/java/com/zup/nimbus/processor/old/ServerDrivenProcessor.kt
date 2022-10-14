package com.zup.nimbus.processor.old

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.zup.nimbus.processor.ClassNames
import com.zup.nimbus.processor.annotation.AutoDeserialize
import com.zup.nimbus.processor.utils.findAnnotations

class ServerDrivenProcessor(private val environment: SymbolProcessorEnvironment): SymbolProcessor {
    private fun createAnnotations(declaration: Sequence<KSAnnotation>): String {
        val instantiation = mutableListOf<String>()
        declaration.forEach {
            if(it.shortName.getShortName() == "Root") instantiation.add("Root()")
            else if(it.shortName.getShortName() == "Ignore") instantiation.add("Ignore()")
            else if(it.shortName.getShortName() == "InjectScope") instantiation.add("InjectScope()")
            else if(it.shortName.getShortName() == "Name") instantiation.add("Name(${it.arguments[0].value})")
        }
        return instantiation.joinToString(", ")
    }

    private fun isComposable(param: KSValueParameter): Boolean {
        return param.type.annotations.find {
            it.shortName.getShortName() == "Composable"
        } != null
    }

    private fun getParamsAsDeserializableProperties(params:  List<KSValueParameter>): List<String> {
        val result = mutableListOf<String>()
        params.forEach { param ->
            if (!isComposable(param)) {
                val resolved = param.type.resolve()
                param.type.modifiers
                result.add("""
                    |DeserializableProperty(
                    |    name = "${param.name?.getShortName()}",
                    |    annotations = listOf(${createAnnotations(param.annotations)}),
                    |    optional = ${param.hasDefault},
                    |    nullable = ${resolved.isMarkedNullable},
                    |    clazz = ${resolved.declaration.simpleName.getShortName()}::class,
                    |    arguments = emptyList(),
                    |)
                """.trimMargin())
            }
        }
        return result
    }

    private fun assignParameters(params: List<KSValueParameter>): String {
        return params.joinToString(", ") {
            if (isComposable(it)) "${it.name?.getShortName()} = data.children"
            else "${it.name?.getShortName()} = deserialized[\"${it.name?.getShortName()}\"] as ${it.type.resolve()}"
        }
    }

    private fun createNimbusComposable(builder: FileSpec.Builder, fn: KSFunctionDeclaration) {
        val paramsAsDeserializableProperties = getParamsAsDeserializableProperties(fn.parameters)
        val fnBuilder = FunSpec.builder(fn.simpleName.getShortName())
            .addAnnotation(ClassNames.Composable)
            .addParameter("data", ClassNames.ComponentData)
            .addStatement("var nimbus = NimbusTheme.nimbus")
            .addStatement("val params = listOf(${paramsAsDeserializableProperties.joinToString(",\n")})")
            .addStatement("val deserializationAttempt = tryToDeserializeProperties(params, AnyServerDrivenData(data.node.properties), data.node)")
            .addCode("""
                |if (deserializationAttempt.error == null) {
                |    val deserialized = deserializationAttempt.result
                |    ${fn.simpleName.getShortName()}(${assignParameters(fn.parameters)})
                |} else if(nimbus.mode == NimbusMode.Development) {
                |  deserializationAttempt.error?.printStackTrace()
                |  Text("Error while deserializing component.", color = Color.Red)
                |}
            """.trimMargin())

        builder.addFunction(fnBuilder.build())
    }

    fun createComponents(packageName: String, functions: List<KSFunctionDeclaration>) {
        val sourceFiles = functions.mapNotNull { it.containingFile }
        val componentsFile = FileSpec.builder(packageName,"generatedComponents")
            .addClassImport(ClassNames.NimbusTheme)
            .addClassImport(ClassNames.NimbusMode)
            .addClassImport(ClassNames.Text)
            .addClassImport(ClassNames.Color)
            .addImport(PackageNames.composeRuntime, "remember")
            .addImport(PackageNames.nimbusCompose, "deserialization.DeserializableProperty")
            .addImport(PackageNames.nimbusCompose, "deserialization.annotation.Ignore")
            .addImport(PackageNames.nimbusCompose, "deserialization.annotation.InjectScope")
            .addImport(PackageNames.nimbusCompose, "deserialization.annotation.Root")
            .addImport(PackageNames.nimbusCompose, "deserialization.annotation.Name")
            .addImport(PackageNames.nimbusCompose, "deserialization.NimbusDeserializer.tryToDeserializeProperties")
            .addImport(PackageNames.nimbusCore, "deserialization.AnyServerDrivenData")

        functions.forEach { fn ->
            fn.parameters.forEach {
                val import = it.type.resolve().declaration
                componentsFile.addImport(import.qualifiedName?.asString() ?: "", "")
            }
            createNimbusComposable(componentsFile, fn)
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
            resolver.findAnnotations(AutoDeserialize::class)
        if (!functions.iterator().hasNext()) return emptyList()

        val byPackage = functions.groupBy { it.packageName.asString() }

        byPackage.forEach {
            createComponents(it.key, it.value)
        }

        return (functions).filterNot { it.validate() }.toList()
    }
}