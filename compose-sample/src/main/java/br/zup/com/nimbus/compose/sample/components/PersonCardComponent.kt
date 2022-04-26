package br.zup.com.nimbus.compose.sample.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import br.zup.com.nimbus.compose.sample.model.PersonCardModel

@Composable
fun PersonCardComponent(params: PersonCardModel) {
    val (hasStarted, setHasStarted) = remember { mutableStateOf(false) }

    SideEffect {
        if (!hasStarted) {
            println("PersonCardComponent is rendered")
            System.out.flush()
            setHasStarted(true)
        }
    }

    if (hasStarted) {
        Column() {
            Text(text = params.person.name)
            Text(text = params.person.age.toString())
            Text(text = params.person.document)
            Text(text = params.person.phone ?: "-")
            Text(text = params.address.street)
            Text(text = params.address.zip)
            Text(text = params.address.number.toString())
        }
    }
}