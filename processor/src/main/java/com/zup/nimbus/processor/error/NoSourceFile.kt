package com.zup.nimbus.processor.error

import com.google.devtools.ksp.symbol.KSDeclaration

class NoSourceFile(declaration: KSDeclaration): DeserializationBuildError(
    "Could not find source file for declaration of  ${declaration.simpleName.asString()}",
)
