package com.zup.nimbus.processor.codegen.function

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.zup.nimbus.processor.model.IdentifiableKSType
import com.zup.nimbus.processor.model.Property

/**
 * A function context stores all the important information for writing the code for the current
 * property under deserialization.
 *
 * This is important so we don't need to pass a lot of parameters from a function to another. This
 * makes the code much simpler.
 */
internal class FunctionWriterContext(
    /**
     * The current property being deserialized
     */
    val property: Property,
    /**
     * The builder for the current function being generated (Kotlin Poet)
     */
    val builder: FunSpec.Builder,
    /**
     * All custom deserializers available. Custom deserializers are functions annotated with
     * `@Deserializer` in the source code.
     */
    val deserializers: List<KSFunctionDeclaration>,
    /**
     * Use this set to add new imports to the current file being generated
     */
    val typesToImport: MutableSet<ClassName>,
    /**
     * Use this set to inform that a new class must be auto-deserialized
     */
    val typesToAutoDeserialize: MutableSet<IdentifiableKSType>,
)
