package br.zup.com.nimbus.compose

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.zup.nimbus.core.tree.ServerDrivenNode

fun <T>ServerDrivenNode.parse(typeRef: TypeReference<T>): T {
    val mapper = jacksonObjectMapper()
    return mapper.convertValue(this.properties, typeRef)
}
