package br.com.zup.nimbus.compose.sample.model

import br.com.zup.nimbus.annotation.Root

class Box(
    @Root val size: Size?,
    @Root val border: Border?,
    val backgroundColor: String?,
    val elevation: Int?,
)
