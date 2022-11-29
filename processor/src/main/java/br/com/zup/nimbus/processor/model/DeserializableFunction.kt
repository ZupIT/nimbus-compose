package br.com.zup.nimbus.processor.model

import com.google.devtools.ksp.symbol.KSFunctionDeclaration

/**
 * A function to undergo auto-deserialization. An action handler, component or operation.
 */
internal class DeserializableFunction(
    /**
     * The declaration of the function.
     */
    val declaration: KSFunctionDeclaration,
    /**
     * The category of the function.
     */
    val category: FunctionCategory,
)
