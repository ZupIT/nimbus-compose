package br.com.zup.nimbus.compose.sample.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import com.zup.nimbus.processor.old.Root
import com.zup.nimbus.processor.annotation.AutoDeserialize

class TextInputEvents(
    val onChange: ((String) -> Unit)?,
    val onFocus: ((String) -> Unit)?,
    val onBlur: ((String) -> Unit)?,
)

enum class TextInputType(val keyboard: KeyboardType) {
    Text(KeyboardType.Text),
    Password(KeyboardType.Password),
    Email(KeyboardType.Email),
    Number(KeyboardType.Number),
}

@Composable
@AutoDeserialize
fun TextInput(
    value: String,
    label: String,
    type: TextInputType? = null,
    enabled: Boolean? = null,
    @Root events: TextInputEvents? = null,
) {
    val modifier = Modifier.onFocusChanged {
        if (it.isFocused) events?.onFocus?.let { it(value) }
        else events?.onBlur?.let { it(value) }
    }

    TextField(
        value = value,
        label = { Text(label) },
        enabled = enabled == true,
        keyboardOptions = KeyboardOptions(keyboardType = (type ?: TextInputType.Text).keyboard),
        onValueChange = { newValue -> events?.onChange?.let { it(newValue) } },
        modifier = modifier,
    )
}
