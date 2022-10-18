package com.zup.nimbus.processor.model

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec

class FunctionWriterResult(
    val typesToImport: Set<ClassName>,
    val typesToAutoDeserialize: Set<IdentifiableKSType>,
    val functionBuilders: List<FunSpec.Builder>,
) {
    constructor(
        typesToImport: Set<ClassName>,
        typesToAutoDeserialize: Set<IdentifiableKSType>,
        functionBuilder: FunSpec.Builder,
    ): this(typesToImport, typesToAutoDeserialize, listOf(functionBuilder))

    companion object {
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

    fun combine(imports: Set<ClassName>): FunctionWriterResult {
        return FunctionWriterResult(typesToImport + imports, typesToAutoDeserialize, functionBuilders)
    }
}
