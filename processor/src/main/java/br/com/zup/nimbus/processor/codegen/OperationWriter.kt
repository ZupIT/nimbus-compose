/*
 * Copyright 2023 ZUP IT SERVICOS EM TECNOLOGIA E INOVACAO SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.zup.nimbus.processor.codegen

import br.com.zup.nimbus.annotation.Root
import br.com.zup.nimbus.processor.ClassNames
import br.com.zup.nimbus.processor.codegen.function.FunctionWriter
import br.com.zup.nimbus.processor.codegen.function.FunctionWriter.CONTEXT_REF
import br.com.zup.nimbus.processor.codegen.function.FunctionWriter.PROPERTIES_REF
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.Variance
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import br.com.zup.nimbus.processor.error.InvalidUseOfComposable
import br.com.zup.nimbus.processor.error.InvalidUseOfContext
import br.com.zup.nimbus.processor.error.InvalidUseOfRoot
import br.com.zup.nimbus.processor.error.UnsupportedFunction
import br.com.zup.nimbus.processor.model.FunctionWriterResult
import br.com.zup.nimbus.processor.model.IndexedProperty
import br.com.zup.nimbus.processor.utils.getQualifiedName
import br.com.zup.nimbus.processor.utils.hasAnnotation

/**
 * Writes the code for any operation annotated with @AutoDeserialize.
 *
 * Operations are identified by the rule: functions that returns something other than Unit.
 */
internal object OperationWriter {
    /**
     * Name of the variable that holds a reference to the list of arguments. It's prefixed with
     * "__" to avoid name clashes with user defined parameters (deserialized properties).
     */
    private const val ARGUMENTS_REF = "__arguments"
    /**
     * Name of the variable that holds a reference to the list of arguments after it has been
     * treated for varargs.
     */
    private const val TREATED_ARGUMENTS_REF = "__treatedArguments"

    /**
     * Set of imports common to every file with a generated operation.
     */
    private val imports = setOf(
        ClassNames.DeserializationContext,
        ClassNames.AnyServerDrivenData,
    )

    /**
     * Checks the operation for invalid parameters. A parameter is invalid if it:
     * - is a function;
     * - is a `DeserializationContext`;
     * - is annotated with `@Root`.
     */
    private fun validate(operation: KSFunctionDeclaration) {
        operation.parameters.forEach {
            if (it.type.hasAnnotation(ClassNames.Composable)) throw InvalidUseOfComposable(it)
            if (it.hasAnnotation<Root>()) throw InvalidUseOfRoot(
                it,
                "@Root can't be used for operations since an operation accepts a list " +
                        "of arguments and not a map."
            )
            val resolvedType = it.type.resolve()
            if (resolvedType.isFunctionType) throw UnsupportedFunction("operation", it)
            if (resolvedType.getQualifiedName() ==
                ClassNames.DeserializationContext.canonicalName) throw InvalidUseOfContext(it)
        }
    }

    /**
     * Makes the function an extension of the parent element if the parent is a class or an object.
     */
    private fun makeAnExtensionIfNeeded(fnBuilder: FunSpec.Builder, parent: KSNode?) {
        if (parent is KSClassDeclaration) {
            fnBuilder.receiver(parent.asStarProjectedType().toTypeName())
        }
    }

    /**
     * Transforms a parameter `vararg name: T` into a property of type `List<T>` so it can be
     * correctly managed by the `FunctionWriter`.
     */
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

    /**
     * If the operation has a vararg parameter, we must treat the incoming argument list so we can
     * correctly assign each argument. Here, we write the code to extract a sublist from the
     * original list of arguments corresponding to the vararg parameter, we then create a new
     * argument list where the position equivalent to the vararg parameter corresponds to the
     * previously extracted sublist.
     */
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

    /**
     * Writes the header of a generated component, i.e. the function declaration and some
     * useful variables.
     */
    private fun writeHeader(operationName: String, returnType: TypeName) =
        FunSpec.builder(operationName)
            .addParameter(
                ARGUMENTS_REF,
                List::class.asTypeName().parameterizedBy(
                    Any::class.asTypeName().copy(nullable = true),
                ),
            )
            .returns(returnType)
            .addStatement("val %L = DeserializationContext()", CONTEXT_REF)

    /**
     * Writes the body of a generated operation, i.e. the deserialization itself, it will have
     * the form:
     *
     * ```
     * val propertyA = properties.at(0).asString()
     * val propertyB = properties.at(1).asIntOrNull()
     * // ...
     * ```
     */
    private fun writeBody(
        operation: KSFunctionDeclaration,
        deserializers: List<KSFunctionDeclaration>,
        fnBuilder: FunSpec.Builder,
        operationName: String,
        resolver: Resolver,
    ): Pair<List<IndexedProperty>, FunctionWriterResult> {
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
        return properties to result
    }

    /**
     * Writes the last part of a generated operation, which calls the original operation
     * with the deserialized parameters or throws an IllegalArgumentException if the operation
     * is unsuccessful.
     */
    private fun writeFooter(
        operationName: String,
        properties: List<IndexedProperty>,
        fnBuilder: FunSpec.Builder,
    ) {
        fnBuilder.addCode(
            """
            |if ($PROPERTIES_REF.hasError()) {
            |  throw IllegalArgumentException(
            |    "Could not deserialize arguments into Operation $operationName. See the errors below:" +
            |            $PROPERTIES_REF.errorsAsString()
            |  )
            |}
            |return $operationName(
            |  ${ParameterUtils.buildParameterAssignments(properties).joinToString(",\n  ")}
            |)
            |""".trimMargin(),
        )
    }

    /**
     * Writes a function that deserializes the properties of an operation.
     */
    fun write(
        operation: KSFunctionDeclaration,
        deserializers: List<KSFunctionDeclaration>,
        resolver: Resolver,
    ): FunctionWriterResult {
        validate(operation)
        val operationName = operation.simpleName.asString()
        val fnBuilder = writeHeader(operationName, operation.returnType!!.toTypeName())
        makeAnExtensionIfNeeded(fnBuilder, operation.parent)
        val (properties, result) = writeBody(operation, deserializers, fnBuilder, operationName, resolver)
        writeFooter(operationName, properties, fnBuilder)
        return result.combine(imports)
    }
}
