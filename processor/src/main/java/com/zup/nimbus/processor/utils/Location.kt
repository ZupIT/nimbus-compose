package com.zup.nimbus.processor.utils

import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.Location

private val fileNameRegex = Regex("[^/]+$")

/**
 * A location in the format (fileName:lineNumber). This can be read by IntelliJ and correctly linked
 * in the file system.
 */
fun Location.toLocationString(): String {
    if (this is FileLocation) {
        val match = fileNameRegex.find(this.filePath)?.groupValues?.firstOrNull()
        if (match != null) return "(${match}:${this.lineNumber})"
    }
    return ""
}
