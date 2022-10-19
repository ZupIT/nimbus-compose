package com.zup.nimbus.processor.error

import com.google.devtools.ksp.symbol.KSDeclaration
import com.zup.nimbus.processor.utils.toLocationString

class NoSourceFile(declaration: KSDeclaration): AutoDeserializationError(
    "Could not find source file for declaration of  ${declaration.simpleName.asString()}" +
            "\n\tdeclaration at " +
            (declaration.qualifiedName?.asString() ?: declaration.simpleName.asString()) +
            declaration.location.toLocationString()
)
