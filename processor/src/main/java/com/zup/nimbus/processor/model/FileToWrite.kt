package com.zup.nimbus.processor.model

import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.FileSpec

class FileToWrite(
    val spec: FileSpec,
    val source: KSFile,
)
