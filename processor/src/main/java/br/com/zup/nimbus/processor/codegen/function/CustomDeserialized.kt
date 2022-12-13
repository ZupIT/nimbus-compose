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

package br.com.zup.nimbus.processor.codegen.function

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName
import br.com.zup.nimbus.processor.ClassNames
import br.com.zup.nimbus.processor.codegen.function.FunctionWriter.CONTEXT_REF
import br.com.zup.nimbus.processor.codegen.function.FunctionWriter.PROPERTIES_REF
import br.com.zup.nimbus.processor.error.DeserializerPossiblyNull
import br.com.zup.nimbus.processor.model.Property
import br.com.zup.nimbus.processor.utils.getQualifiedName
import br.com.zup.nimbus.processor.utils.hasSameArguments

/**
 * Writes the code for deserializing a property into an instance of a class that is custom
 * deserialized, i.e. a class that has a deserialization function associated to it (@Deserializer).
 * The opposite of an auto-deserialized class.
 */
internal object CustomDeserialized {
    private fun validate(
        property: Property,
        deserializer: KSFunctionDeclaration,
        type: KSType = property.type,
    ) {
        if (!type.isMarkedNullable &&
            deserializer.returnType?.resolve()?.isMarkedNullable == true) {
            throw DeserializerPossiblyNull(property, deserializer)
        }
    }

    fun findDeserializer(
        type: KSType,
        deserializers: List<KSFunctionDeclaration>,
    ): KSFunctionDeclaration? = deserializers.find {
        val returnType = it.returnType?.resolve()
        returnType?.let { rtype ->
            rtype.getQualifiedName() == type.getQualifiedName() && type.hasSameArguments(rtype)
        } ?: false
    }

    fun findDeserializer(ctx: FunctionWriterContext): KSFunctionDeclaration? = findDeserializer(
        ctx.property.type,
        ctx.deserializers,
    )

    fun getCallString(
        ctx: FunctionWriterContext,
        deserializer: KSFunctionDeclaration,
        type: KSType,
        propertiesRef: String = PROPERTIES_REF,
        contextRef: String = CONTEXT_REF,
    ): String {
        validate(ctx.property, deserializer, type)
        val name = deserializer.simpleName.asString()
        ctx.typesToImport.add(
            ClassName(deserializer.packageName.asString(), name)
        )
        val paramString = deserializer.parameters.joinToString(", ") {
            if (it.type.resolve().getQualifiedName() ==
                ClassNames.DeserializationContext.canonicalName) contextRef
            else propertiesRef
        }
        return "${name}($paramString)"
    }

    fun getCallString(
        ctx: FunctionWriterContext,
        deserializer: KSFunctionDeclaration,
        propertiesRef: String = PROPERTIES_REF,
        contextRef: String = CONTEXT_REF,
    ): String = getCallString(ctx, deserializer, ctx.property.type, propertiesRef, contextRef)

    fun write(ctx: FunctionWriterContext, deserializer: KSFunctionDeclaration) {
        ctx.builder.addStatement(
            "val %L = %L",
            ctx.property.name,
            getCallString(
                ctx,
                deserializer,
                ctx.property.getAccessString(PROPERTIES_REF),
            ),
        )
        ctx.typesToImport.add(ClassName(
            deserializer.packageName.asString(),
            deserializer.simpleName.asString(),
        ))
    }
}