package com.zup.nimbus.processor.model

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec

/**
 * The result of a process that writes one or more functions.
 */
internal class FunctionWriterResult(
    /**
     * All types that needs to be imported in order for the functions to work.
     */
    val typesToImport: Set<ClassName>,
    /**
     * All classes that didn't have a custom deserializer and need to be automatically deserialized
     * in order for the functions to work.
     */
    val typesToAutoDeserialize: Set<IdentifiableKSType>,
    /**
     * The generated functions.
     */
    val functionBuilders: List<FunSpec.Builder>,
) {
    constructor(
        typesToImport: Set<ClassName>,
        typesToAutoDeserialize: Set<IdentifiableKSType>,
        functionBuilder: FunSpec.Builder,
    ): this(typesToImport, typesToAutoDeserialize, listOf(functionBuilder))

    companion object {
        /**
         * Combines multiple results into one.
         */
        fun combine(list: List<FunctionWriterResult>): FunctionWriterResult {
            val combinedTypesTtoImport = mutableSetOf<ClassName>()
            val combinedTypesToAutoDeserialize = mutableSetOf<IdentifiableKSType>()
            val combinedFunctionBuilders = mutableListOf<FunSpec.Builder>()
            list.forEach {
                combinedTypesTtoImport.addAll(it.typesToImport)
                combinedTypesToAutoDeserialize.addAll(it.typesToAutoDeserialize)
                combinedFunctionBuilders.addAll(it.functionBuilders)
            }
            return FunctionWriterResult(
                combinedTypesTtoImport,
                combinedTypesToAutoDeserialize,
                combinedFunctionBuilders,
            )
        }
    }

    /**
     * Combines the imports of this result with a set of imports, returning a new FunctionResult.
     */
    fun combine(imports: Set<ClassName>): FunctionWriterResult {
        return FunctionWriterResult(typesToImport + imports, typesToAutoDeserialize, functionBuilders)
    }
}
