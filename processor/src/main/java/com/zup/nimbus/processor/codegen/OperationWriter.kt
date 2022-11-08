package com.zup.nimbus.processor.codegen

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.Variance
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.zup.nimbus.processor.ClassNames
import br.com.zup.nimbus.annotation.Root
import com.zup.nimbus.processor.codegen.function.FunctionWriter
import com.zup.nimbus.processor.codegen.function.FunctionWriter.CONTEXT_REF
import com.zup.nimbus.processor.codegen.function.FunctionWriter.PROPERTIES_REF
import com.zup.nimbus.processor.error.InvalidUseOfComposable
import com.zup.nimbus.processor.error.InvalidUseOfContext
import com.zup.nimbus.processor.error.InvalidUseOfRoot
import com.zup.nimbus.processor.model.FunctionWriterResult
import com.zup.nimbus.processor.model.IndexedProperty
import com.zup.nimbus.processor.utils.getQualifiedName
import com.zup.nimbus.processor.utils.hasAnnotation

internal object OperationWriter {
    private const val ARGUMENTS_REF = "__arguments"
    private const val TREATED_ARGUMENTS_REF = "__treatedArguments"

    private val imports = setOf(
        ClassNames.DeserializationContext,
        ClassNames.AnyServerDrivenData,
    )

    private fun validate(operation: KSFunctionDeclaration) {
        operation.parameters.forEach {
            if (it.type.hasAnnotation(ClassNames.Composable)) throw InvalidUseOfComposable(it)
            if (it.hasAnnotation<Root>()) throw InvalidUseOfRoot(
                it,
                "@Root can't be used for operations since an operation accepts a list " +
                        "of arguments and not a map."
            )
            if (it.type.resolve().getQualifiedName() ==
                ClassNames.DeserializationContext.canonicalName) throw InvalidUseOfContext(it)
        }
    }

    private fun transformVarArgTypeToList(
        property: IndexedProperty,
        resolver: Resolver,
    ): IndexedProperty {
        val ksList = resolver.getClassDeclarationByName(List::class.qualifiedName!!)!!
        val typeArgument = object: KSTypeArgument, KSAnnotated by property.typeReference {
            override val type = property.typeReference
            override val variance = Variance.COVARIANT
        }

        val listType = object: KSType by property.type {
            override val arguments = listOf(typeArgument)
            override val declaration = ksList
        }

        return IndexedProperty(
            property.name,
            listType,
            property.typeReference,
            property.category,
            property.location,
            property.parent,
            property.index,
            property.isVararg
        )
    }

    private fun treatVarArgs(
        properties: List<IndexedProperty>,
        builder: FunSpec.Builder,
        resolver: Resolver,
        operationName: String,
    ): List<IndexedProperty> {
        val varArgIndex = properties.indexOfFirst { it.isVararg }
        val paramsAfterVararg = properties.size - varArgIndex - 1
        if (varArgIndex == -1) {
            builder.addStatement("val %L = %L", TREATED_ARGUMENTS_REF, ARGUMENTS_REF)
            return properties
        }
        else {
            builder.addCode(
                """
                |val $TREATED_ARGUMENTS_REF = try {
                |  $ARGUMENTS_REF.subList(0, $varArgIndex) +
                |        listOf($ARGUMENTS_REF.subList($varArgIndex, $ARGUMENTS_REF.size - $paramsAfterVararg)) +
                |        $ARGUMENTS_REF.subList($ARGUMENTS_REF.size - $paramsAfterVararg, $ARGUMENTS_REF.size)
                |} catch (e: IndexOutOfBoundsException) {
                |  throw IllegalArgumentException("Could not deserialize arguments into Operation " +
                |      "$operationName because it received an insufficient number of arguments")
                |}
                |""".trimMargin()
            )
            val treated = properties.toMutableList()
            treated[varArgIndex] = transformVarArgTypeToList(treated[varArgIndex], resolver)
            return treated
        }
    }

    fun write(
        operation: KSFunctionDeclaration,
        deserializers: List<KSFunctionDeclaration>,
        resolver: Resolver,
    ): FunctionWriterResult {
        validate(operation)
        val operationName = operation.simpleName.asString()
        val fnBuilder = FunSpec.builder(operationName)
            .addParameter(
                ARGUMENTS_REF,
                List::class.asTypeName().parameterizedBy(
                    Any::class.asTypeName().copy(nullable = true),
                ),
            )
            .returns(operation.returnType!!.toTypeName())
            .addStatement("val %L = DeserializationContext()", CONTEXT_REF)
        val properties = treatVarArgs(
            ParameterUtils.convertParametersIntoIndexedProperties(operation.parameters),
            fnBuilder,
            resolver,
            operationName,
        )
        fnBuilder.addStatement(
            "val %L = AnyServerDrivenData(%L)",
            PROPERTIES_REF,
            TREATED_ARGUMENTS_REF,
        )
        val result = FunctionWriter.write(properties, deserializers, fnBuilder)
        fnBuilder.addCode(
            """
            |if ($PROPERTIES_REF.hasError()) {
            |  throw IllegalArgumentException(
            |    "Could not deserialize arguments into Operation $operationName. See the errors below:" +
            |            $PROPERTIES_REF.errorsAsString()
            |  )
            |}
            |return ${operation.simpleName.asString()}(
            |  ${ParameterUtils.buildParameterAssignments(properties).joinToString(",\n  ")}
            |)
            |""".trimMargin(),
        )

        return result.combine(imports)
    }
}
