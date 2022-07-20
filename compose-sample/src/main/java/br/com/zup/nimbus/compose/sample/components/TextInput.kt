package br.com.zup.nimbus.compose.sample.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.com.zup.nimbus.compose.sample.BuildConfig
import br.zup.com.nimbus.compose.ComponentData
import br.zup.com.nimbus.compose.NimbusMode
import br.zup.com.nimbus.compose.NimbusTheme
import com.zup.nimbus.core.deserialization.ComponentDeserializer
import com.zup.nimbus.processor.Computed
import com.zup.nimbus.processor.Ignore
import com.zup.nimbus.processor.ParentName
import com.zup.nimbus.processor.RootProperty
import com.zup.nimbus.processor.ServerDrivenComponent
import com.zup.nimbus.processor.TypeDeserializer

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

class TextInputSize(
    val width: Double?,
    val height: Double?,
)

object AnotherTranslator: TypeDeserializer<String> {
    override fun deserialize(data: Any): String {
        return "test"
    }
}

@Composable
@ServerDrivenComponent
fun TextInput(
    value: String,
    label: String,
    @ParentName parent: String?,
    type: TextInputType? = null,
    enabled: Boolean? = null,
    @Ignore size: TextInputSize? = null,
    events: TextInputEvents? = null,
    @Computed<AnotherTranslator>(AnotherTranslator::class) another: String? = null,
) {
    var modifier = Modifier.onFocusChanged {
        if (it.isFocused) events?.onFocus?.let { it(value) }
        else events?.onBlur?.let { it(value) }
    }

    if (size?.width != null) modifier =  modifier.width(size.width.dp)
    if (size?.height != null) modifier =  modifier.height(size.height.dp)

    TextField(
        value = value,
        label = { Text(label) },
        enabled = enabled == true,
        keyboardOptions = KeyboardOptions(keyboardType = (type ?: TextInputType.Text).keyboard),
        onValueChange = { newValue -> events?.onChange?.let { it(newValue) } },
        modifier = modifier,
    )
}

/*@Composable
fun TextInput(data: ComponentData) {
    val nimbus = NimbusTheme.nimbus
    // fixme: !!
    val properties = remember { ComponentDeserializer(logger = nimbus.logger!!, node = data.node) }
    properties.start()
    val value = properties.asString("value")
    val label = properties.asString("label")
    val type = properties.asEnumOrNull("type", TextInputType.values()) ?: TextInputType.Text
    val enabled = properties.asBooleanOrNull("enabled") ?: true
    val isSuccessful = properties.end()
    if (isSuccessful) {
        TextInput(value = value, label = label, type = type, enabled = enabled)
    } else if (nimbus.mode == NimbusMode.Development) {
        Text("Error while deserializing ${data.node.component}.", color = Color.Red)
    }
}*/
