import DocumentType
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import br.zup.com.nimbus.compose.ComponentData
import br.zup.com.nimbus.compose.NimbusMode
import br.zup.com.nimbus.compose.NimbusTheme
import br.zup.com.nimbus.compose.deserialization.DeserializationContext
import com.zup.nimbus.core.deserialization.AnyServerDrivenData
import createDocumentFromAnyServerDrivenData
import createMyComponentDataFromAnyServerDrivenData
import deserializeMyDate
import kotlin.Unit

@Composable
public fun LeafComponent(__component: ComponentData): Unit {
  val __context = DeserializationContext(__component)
  val __properties = AnyServerDrivenData(__component.node.properties)
  val name = __properties.get("name").asStringOrNull()
  val date = deserializeMyDate(__properties.get("date"))
  val metadata = __properties.get("metadata").value ?: Any()
  val age = __properties.get("age").asIntOrNull()
  val __onPressEvent = __properties.get("onPress").asEvent()
  val onPress = { __onPressEvent.run() }
  val documents = __properties.get("documents").asList().map {
      createDocumentFromAnyServerDrivenData(it, __context) }
  if (!__properties.hasError()) {
    LeafComponent(
      name = name,
      date = date,
      metadata = metadata,
      age = age,
      onPress = onPress,
      documents = documents
    )
  } else if (NimbusTheme.nimbus.mode == NimbusMode.Development) {
    NimbusTheme.nimbus.logger.error(
      "Can't deserialize properties of the component with id ${__component.node.id} " +
              "into the composable LeafComponent. See the errors below:" +
              __properties.errorsAsString()
    )
    Text("Error while deserializing component. Check the logs for details.", color = Color.Red)
  }
}

@Composable
public fun ContainerComponent(__component: ComponentData): Unit {
  val __context = DeserializationContext(__component)
  val __properties = AnyServerDrivenData(__component.node.properties)
  val content = __context.component?.children ?: { Column {} }
  if (!__properties.hasError()) {
    ContainerComponent(
      content = content
    )
  } else if (NimbusTheme.nimbus.mode == NimbusMode.Development) {
    NimbusTheme.nimbus.logger.error(
      "Can't deserialize properties of the component with id ${__component.node.id} " +
              "into the composable ContainerComponent. See the errors below:" +
              __properties.errorsAsString()
    )
    Text("Error while deserializing component. Check the logs for details.", color = Color.Red)
  }
}

@Composable
public fun ContextAwareComponent(__component: ComponentData): Unit {
  val __context = DeserializationContext(__component)
  val __properties = AnyServerDrivenData(__component.node.properties)
  if (!__properties.hasError()) {
    ContextAwareComponent(
      context = __context
    )
  } else if (NimbusTheme.nimbus.mode == NimbusMode.Development) {
    NimbusTheme.nimbus.logger.error(
      "Can't deserialize properties of the component with id ${__component.node.id} " +
              "into the composable ContextAwareComponent. See the errors below:" +
              __properties.errorsAsString()
    )
    Text("Error while deserializing component. Check the logs for details.", color = Color.Red)
  }
}

@Composable
public fun ClassPropsComponent(__component: ComponentData): Unit {
  val __context = DeserializationContext(__component)
  val __properties = AnyServerDrivenData(__component.node.properties)
  val data = createMyComponentDataFromAnyServerDrivenData(__properties.get("data"), __context)
  if (!__properties.hasError()) {
    ClassPropsComponent(
      data = data
    )
  } else if (NimbusTheme.nimbus.mode == NimbusMode.Development) {
    NimbusTheme.nimbus.logger.error(
      "Can't deserialize properties of the component with id ${__component.node.id} " +
              "into the composable ClassPropsComponent. See the errors below:" +
              __properties.errorsAsString()
    )
    Text("Error while deserializing component. Check the logs for details.", color = Color.Red)
  }
}

public fun createDocumentFromAnyServerDrivenData(__properties: AnyServerDrivenData,
    __context: DeserializationContext): Document {
  val type = __properties.get("type").asEnum(DocumentType.values())
  val value = __properties.get("value").asString()
  return Document(
    type = type,
    value = value
  )
}

public fun createMyComponentDataFromAnyServerDrivenData(__properties: AnyServerDrivenData,
    __context: DeserializationContext): MyComponentData {
  val content = __context.component?.children ?: { Column {} }
  return MyComponentData(
    context = __context,
    content = content
  )
}
