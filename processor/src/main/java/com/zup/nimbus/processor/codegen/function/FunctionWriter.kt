package com.zup.nimbus.processor.codegen.function

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.zup.nimbus.processor.model.FunctionWriterResult
import com.zup.nimbus.processor.model.IdentifiableKSType
import com.zup.nimbus.processor.model.Property
import com.zup.nimbus.processor.model.PropertyCategory

internal object FunctionWriter {
    private fun writeProperty(ctx: FunctionWriterContext) {
        when (ctx.property.category) {
            PropertyCategory.Composable -> Component.write(ctx)
            PropertyCategory.Context -> {} // skip
            PropertyCategory.Root -> Root.write(ctx)
            PropertyCategory.Common -> CommonProperty.write(ctx)
        }
    }

    fun write(
        parameters: List<Property>,
        deserializers: List<KSFunctionDeclaration>,
        fnBuilder: FunSpec.Builder,
    ): FunctionWriterResult {
        val typesToImport = mutableSetOf<ClassName>()
        val typesToAutoDeserialize = mutableSetOf<IdentifiableKSType>()
        parameters.forEach {
            writeProperty(FunctionWriterContext(
                property = it,
                typesToImport = typesToImport,
                typesToAutoDeserialize = typesToAutoDeserialize,
                builder = fnBuilder,
                deserializers = deserializers,
            ))
        }
        return FunctionWriterResult(typesToImport, typesToAutoDeserialize, fnBuilder)
    }
}