import test.TestResult
import br.com.zup.nimbus.annotation.AutoDeserialize
import br.com.zup.nimbus.annotation.Deserializer
import com.zup.nimbus.core.deserialization.AnyServerDrivenData
import br.zup.com.nimbus.compose.deserialization.DeserializationContext
import androidx.compose.runtime.Composable


import br.zup.com.nimbus.compose.ComponentData
import test.utils.MockNode
import test.utils.MockAction
import test.utils.MockEvent
import com.zup.nimbus.core.ActionTriggeredEvent

class MyTest {
    @AutoDeserialize
    @Composable
    fun Component(value: String) {}
    
    @AutoDeserialize
    fun action(value: String) {}
    
    @AutoDeserialize
    fun operation(value: String) = 0
}

/* the code below will make the compilation fail in case the functions above annotated
with @AutoDeserializable are not generated as extensions of the class MyTest. */

@Composable
fun TestComponent() {
    MyTest().Component(ComponentData(MockNode(), @Composable {}))
}

fun testAction() {
    MyTest().action(ActionTriggeredEvent(MockAction(), MockEvent(emptyList()), mutableSetOf()))
}

fun testOperation() {
    MyTest().operation(emptyList())
}
