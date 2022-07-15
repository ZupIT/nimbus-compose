package br.com.zup.nimbus.compose.sample.components

import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged

typealias InputCallback = (value: String) -> Unit

@Composable
fun TextInput(
    label: String,
    value: String?,
    onChange: InputCallback? = null,
    onBlur: InputCallback? = null,
    onFocus: InputCallback? = null,
) {
    TextField(
        value = value ?: "",
        onValueChange = {
            if (onChange != null) onChange(it)
        },
        label = { Text(label) },
        modifier = Modifier.onFocusChanged {
            if (it.isFocused && onFocus != null) onFocus(value ?: "")
            else if (onBlur != null) onBlur(value ?: "")
        }
    )
}