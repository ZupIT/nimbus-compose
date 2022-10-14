package com.zup.nimbus.processor.old

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec

fun FileSpec.Builder.addClassImport(className: ClassName): FileSpec.Builder {
    this.addImport(className.packageName, className.simpleName)
    return this
}
