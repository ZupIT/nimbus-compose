package br.com.zup.nimbus.compose.sample.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import br.com.zup.nimbus.compose.sample.model.Address
import br.com.zup.nimbus.compose.sample.model.Person

// TODO: Support auto deserialization for this component and register it
@Composable
fun PersonCardComponent(person: Person, address: Address) {
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
            Text(text = person.name)
            Text(text = person.age.toString())
            Text(text = person.document)
            Text(text = person.phone ?: "-")
            Text(text = address.street)
            Text(text = address.zip)
            Text(text = address.number.toString())
        }
    }
}
