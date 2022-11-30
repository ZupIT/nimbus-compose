package br.com.zup.nimbus.processor.codegen

import br.com.zup.nimbus.processor.ClassNames
import br.com.zup.nimbus.processor.codegen.function.FunctionWriter
import br.com.zup.nimbus.processor.codegen.function.FunctionWriter.CONTEXT_REF
import br.com.zup.nimbus.processor.codegen.function.FunctionWriter.PROPERTIES_REF
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Visibility
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import br.com.zup.nimbus.processor.error.UnsupportedDeserialization
import br.com.zup.nimbus.processor.model.FunctionWriterResult
import br.com.zup.nimbus.processor.model.Property
import br.com.zup.nimbus.processor.utils.getSimpleName

/**
 * Writes a function to deserialize a class that doesn't have an assigned custom deserializer.
 *
 * These classes are discovered by a component, action handler or operation annotated with
 * `@AutoDeserialize` that uses this type in its parameters. This is also a deep search, if a
 * class A is found this way, and it references a class B in its constructor, B will also be
 * auto-deserialized if it doesn't have a custom deserializer.
 */
internal object EntityWriter {
    private fun copyTypeVisibilityToFunctionVisibility(type: KSType, fnBuilder: FunSpec.Builder) {
       when(type.declaration.getVisibility()) {
           Visibility.INTERNAL -> fnBuilder.addModifiers(KModifier.INTERNAL)
           Visibility.PRIVATE -> fnBuilder.addModifiers(KModifier.PRIVATE)
           Visibility.PROTECTED -> fnBuilder.addModifiers(KModifier.PROTECTED)
           else -> {}
       }
    }

    /**
     * The name generated for the function that will deserialize the class.
     */
    fun createFunctionName(type: KSType): String {
        return "create${type.getSimpleName()}FromAnyServerDrivenData"
    }

    /**
     * Writes a function that deserializes the properties of a class.
     */
    fun write(type: KSType, deserializers: List<KSFunctionDeclaration>): FunctionWriterResult {
        /**
         * Writes the deserializer.
         */
        fun writeDeserializationFunction(
            className: ClassName,
            properties: List<Property>,
        ): FunctionWriterResult {
            val fnBuilder = FunSpec.builder(createFunctionName(type))
                .returns(className)
                .addParameter(PROPERTIES_REF, ClassNames.AnyServerDrivenData)
                .addParameter(CONTEXT_REF, ClassNames.DeserializationContext)
            val result = FunctionWriter.write(properties, deserializers, fnBuilder)
            fnBuilder.addCode(
                """
                |return %L(
                |  %L
                |)
                """.trimMargin(),
                className.simpleName,
                ParameterUtils.buildParameterAssignments(properties).joinToString(",\n  ")
            )
            copyTypeVisibilityToFunctionVisibility(type, fnBuilder)
            return result
        }

        /**
         * Tries to find a viable constructor and extract its parameters before writing the
         * deserializer.
         */
        fun writeClassDeserializer(declaration: KSClassDeclaration): FunctionWriterResult {
            val constructor = if (declaration.primaryConstructor?.parameters?.isEmpty() == false) {
                declaration.primaryConstructor!!
            } else {
                throw UnsupportedDeserialization(
                    type,
                    "this class doesn't have a primary constructor that accepts parameters",
                )
            }
            val properties =
                ParameterUtils.convertParametersIntoNamedProperties(constructor.parameters)
            val className = ClassName(
                declaration.packageName.asString(),
                declaration.simpleName.asString(),
            )
            return writeDeserializationFunction(className, properties)
        }

        fun main(): FunctionWriterResult {
            val declaration = type.declaration
            if (declaration is KSClassDeclaration) {
                return writeClassDeserializer(declaration)
            } else {
                throw UnsupportedDeserialization(type, "it's not a class")
            }
        }

        return main()
    }
}
