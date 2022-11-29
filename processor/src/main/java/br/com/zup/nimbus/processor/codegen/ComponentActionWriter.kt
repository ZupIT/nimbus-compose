package br.com.zup.nimbus.processor.codegen

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.toTypeName
import br.com.zup.nimbus.processor.codegen.function.FunctionWriter
import br.com.zup.nimbus.processor.model.FunctionWriterResult
import br.com.zup.nimbus.processor.model.NamedProperty

/**
 * Common code for the ComponentWriter and ActionWriter
 */
internal abstract class ComponentActionWriter {
    /**
     * Set of imports common to every file with a generated action handler or component.
     */
    protected abstract val imports: Set<ClassName>

    /**
     * Validates the parameters in the function declaration.
     */
    protected open fun validate(declaration: KSFunctionDeclaration) {}

    /**
     * Writes the header of a generated action handler or component, i.e. the function declaration
     * and some useful variables.
     */
    protected abstract fun writeHeader(name: String): FunSpec.Builder

    /**
     * Makes the function an extension of the parent element if the parent is a class or an object.
     */
    private fun makeAnExtensionIfNeeded(fnBuilder: FunSpec.Builder, parent: KSNode?) {
        if (parent is KSClassDeclaration) {
            fnBuilder.receiver(parent.asStarProjectedType().toTypeName())
        }
    }

    /**
     * Writes the body of a generated action handler or component, i.e. the deserialization itself,
     * it will have the form:
     *
     * ```
     * val propertyA = properties.get("propertyA").asString()
     * val propertyB = properties.get("propertyB").asIntOrNull()
     * // ...
     * ```
     */
    private fun writeBody(
        action: KSFunctionDeclaration,
        deserializers: List<KSFunctionDeclaration>,
        fnBuilder: FunSpec.Builder,
    ): Pair<List<NamedProperty>, FunctionWriterResult> {
        val properties = ParameterUtils.convertParametersIntoNamedProperties(action.parameters)
        val result = FunctionWriter.write(properties, deserializers, fnBuilder)
        return properties to result
    }

    /**
     * Writes the last part of a generated component or action handler, which calls the original
     * function with the deserialized parameters, unless a deserialization error happens. In case of
     * error, if it's an action handler, it throws an IllegalArgumentException; if it's a component
     * it logs the error and renders a Text message (depending on the environment).
     */
    protected abstract fun writeFooter(
        name: String,
        properties: List<NamedProperty>,
        fnBuilder: FunSpec.Builder,
    )

    /**
     * Writes a function that deserializes the properties of a component or action handler.
     */
    fun write(
        declaration: KSFunctionDeclaration,
        deserializers: List<KSFunctionDeclaration>,
    ): FunctionWriterResult {
        validate(declaration)
        val actionName = declaration.simpleName.asString()
        val fnBuilder = writeHeader(actionName)
        makeAnExtensionIfNeeded(fnBuilder, declaration.parent)
        val (properties, result) = writeBody(declaration, deserializers, fnBuilder)
        writeFooter(actionName, properties, fnBuilder)
        return result.combine(imports)
    }
}
