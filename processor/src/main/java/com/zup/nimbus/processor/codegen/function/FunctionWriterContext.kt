package com.zup.nimbus.processor.codegen.function

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.zup.nimbus.processor.model.IdentifiableKSType
import com.zup.nimbus.processor.model.Property

internal class FunctionWriterContext(
    val property: Property,
    val builder: FunSpec.Builder,
    val deserializers: List<KSFunctionDeclaration>,
    val typesToImport: MutableSet<ClassName>,
    val typesToAutoDeserialize: MutableSet<IdentifiableKSType>,
)
