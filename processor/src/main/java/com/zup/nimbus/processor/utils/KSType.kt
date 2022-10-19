package com.zup.nimbus.processor.utils

import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import com.zup.nimbus.processor.error.InvalidListType
import com.zup.nimbus.processor.error.InvalidMapType

fun KSType.getQualifiedName(): String? = this.declaration.qualifiedName?.asString()
fun KSType.getSimpleName(): String = this.declaration.simpleName.asString()
fun KSType.getPackageName(): String = this.declaration.packageName.asString()

fun KSType.isAny(): Boolean = this.getQualifiedName() == Any::class.qualifiedName
fun KSType.isString(): Boolean = this.getQualifiedName() == String::class.qualifiedName
fun KSType.isBoolean(): Boolean = this.getQualifiedName() == Boolean::class.qualifiedName
fun KSType.isInt(): Boolean = this.getQualifiedName() == Int::class.qualifiedName
fun KSType.isLong(): Boolean = this.getQualifiedName() == Long::class.qualifiedName
fun KSType.isDouble(): Boolean = this.getQualifiedName() == Double::class.qualifiedName
fun KSType.isFloat(): Boolean = this.getQualifiedName() == Float::class.qualifiedName
fun KSType.isMap(): Boolean = this.getQualifiedName() == Map::class.qualifiedName
fun KSType.isList(): Boolean = this.getQualifiedName() == List::class.qualifiedName
fun KSType.isEnum(): Boolean = this.declaration.modifiers.contains(Modifier.ENUM)
fun KSType.isPrimitive(): Boolean = this.isString() || this.isBoolean() || this.isInt()
        || this.isLong() || this.isDouble() || this.isFloat()
fun KSType.isKnown(): Boolean = this.isPrimitive() || this.isList() || this.isMap()
        || this.isEnum() || this.isFunctionType

fun KSType.resolveListType(): KSType? =
    this.arguments.firstOrNull()?.type?.resolve()

fun KSType.resolveMapType(): Pair<KSType?, KSType?> {
    val keyType = this.arguments.firstOrNull()?.type?.resolve()
    val valueType = this.arguments.getOrNull(1)?.type?.resolve()
    return Pair(keyType, valueType)
}

fun KSType.hasSameArguments(other: KSType): Boolean {
    if (this.arguments.size != other.arguments.size) return false
    this.arguments.forEachIndexed { index, current ->
        val currentType = current.type?.resolve()
        val otherType = other.arguments.elementAt(index).type?.resolve()
        if (currentType?.getQualifiedName() != otherType?.getQualifiedName()) return false
    }
    return true
}
