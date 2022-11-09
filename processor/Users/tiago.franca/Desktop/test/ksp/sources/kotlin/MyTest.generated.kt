import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import br.zup.com.nimbus.compose.ComponentData
import br.zup.com.nimbus.compose.NimbusMode
import br.zup.com.nimbus.compose.NimbusTheme
import br.zup.com.nimbus.compose.deserialization.DeserializationContext
import com.zup.nimbus.core.ActionTriggeredEvent
import com.zup.nimbus.core.deserialization.AnyServerDrivenData
import kotlin.Any
import kotlin.Int
import kotlin.Unit
import kotlin.collections.List

@Composable
public fun MyTest.Component(__component: ComponentData): Unit {
  val __context = DeserializationContext(__component)
  val __properties = AnyServerDrivenData(__component.node.properties)
  val value = __properties.get("value").asString()
  if (!__properties.hasError()) {
    Component(
      value = value
    )
  } else if (NimbusTheme.nimbus.mode == NimbusMode.Development) {
    NimbusTheme.nimbus.logger.error(
      "Can't deserialize properties of the component with id ${__component.node.id} " +
              "into the composable Component. See the errors below:" +
              __properties.errorsAsString()
    )
    Text("Error while deserializing component. Check the logs for details.", color = Color.Red)
  }
}

public fun MyTest.action(__event: ActionTriggeredEvent): Unit {
  val __context = DeserializationContext(null, __event)
  val __properties = AnyServerDrivenData(__event.action.properties)
  val value = __properties.get("value").asString()
  if (__properties.hasError()) {
    throw IllegalArgumentException(
       "Can't deserialize properties of the action ${__event.action.name} in the event " +
              "${__event.scope.name} of the component with id ${__event.scope.node.id} " +
              "into the Action Handler action. See the errors below:" +
              __properties.errorsAsString()
    )
  }
  action(
    value = value
  )
}

public fun MyTest.operation(__arguments: List<Any?>): Int {
  val __context = DeserializationContext()
  val __treatedArguments = __arguments
  val __properties = AnyServerDrivenData(__treatedArguments)
  val value = __properties.at(0).asString()
  if (__properties.hasError()) {
    throw IllegalArgumentException(
      "Could not deserialize arguments into Operation operation. See the errors below:" +
              __properties.errorsAsString()
    )
  }
  return operation(
    value = value
  )
}
