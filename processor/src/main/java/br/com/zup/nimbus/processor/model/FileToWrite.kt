package br.com.zup.nimbus.processor.model

import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.FileSpec

/**
 * A file to write to the file system using the KSP tool.
 */
internal class FileToWrite(
    /**
     * The file specification (content and name) from Kotlin Poet.
     */
    val spec: FileSpec,
    /**
     * The source file used to generate this file. If no source file was used, this is null.
     */
    val source: KSFile?,
)
