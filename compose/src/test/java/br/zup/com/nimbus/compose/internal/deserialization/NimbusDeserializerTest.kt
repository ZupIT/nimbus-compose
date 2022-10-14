package br.zup.com.nimbus.compose.internal.deserialization

import br.zup.com.nimbus.compose.deserialization.NimbusDeserializer
import com.zup.nimbus.processor.annotation.Root
import com.zup.nimbus.core.ActionTriggeredEvent
import com.zup.nimbus.core.Nimbus
import com.zup.nimbus.core.ServerDrivenConfig
import com.zup.nimbus.core.deserialization.AnyServerDrivenData
import com.zup.nimbus.core.scope.closestState
import com.zup.nimbus.core.tree.dynamic.DynamicAction
import com.zup.nimbus.core.tree.dynamic.DynamicEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

enum class Genre {
    Male,
    Female,
    Other,
}

class Person {
    var id: String = ""
    var name: String = ""
    var birthDate: Int? = null
    var documents: List<Document>? = null
    @com.zup.nimbus.processor.annotation.Root
    var parents: ParentsData? = null
    var onPress: (() -> Unit)? = null
    var onChange: ((String) -> Unit)? = null
    var genre: Genre? = null
}

class ParentsData(
    val mother: Person,
    val father: Person,
)

class Document(
    val name: String,
    val value: String,
)

val johannaProps = mapOf<String, Any?>(
    "id" to "001",
    "name" to "Johanna Stone",
)

val paulProps = mapOf<String, Any?>(
    "id" to "002",
    "name" to "Paul Stone",
)

val onPress = DynamicEvent("onPress")
val onChange = DynamicEvent("onChange")

val johnProps = mapOf<String, Any?>(
    "id" to "003",
    "name" to "John Stone",
    "birthDate" to 47855554,
    "documents" to listOf(
        mapOf("name" to "ssn", "value" to "47855110"),
        mapOf("name" to "drivers license", "value" to "11100023"),
    ),
    "mother" to johannaProps,
    "father" to paulProps,
    "onPress" to onPress,
    "onChange" to onChange,
    "genre" to "male",
)

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GenericClassDeserializerTest {
    var action1Called = false
    var action2CalledWith: ActionTriggeredEvent? = null

    @BeforeAll
    fun createEvents() {
        onPress.actions = listOf(
            DynamicAction("test1", handler = { action1Called = true }, initHandler = null)
        )
        onChange.actions = listOf(
            DynamicAction("test2", handler = { action2CalledWith = it }, initHandler = null)
        )
    }

    @BeforeEach
    fun reset() {
        action1Called = false
        action2CalledWith = null
    }

    @Test
    fun test() {
        val scope = Nimbus(ServerDrivenConfig(baseUrl = "", platform = "test"))
        val data = AnyServerDrivenData(johnProps)
        val person = NimbusDeserializer.deserialize(data, scope, Person::class)
        assertEquals(johnProps["id"], person?.id)
        assertEquals(johnProps["name"], person?.name)
        assertEquals(johnProps["birthDate"], person?.birthDate)
        assertEquals(Genre.Male, person?.genre)
        assertEquals(2, person?.documents?.size)
        assertEquals("ssn", person?.documents?.get(0)?.name)
        assertEquals("47855110", person?.documents?.get(0)?.value)
        assertEquals("drivers license", person?.documents?.get(1)?.name)
        assertEquals("11100023", person?.documents?.get(1)?.value)
        assertEquals(johannaProps["id"], person?.parents?.mother?.id)
        assertEquals(johannaProps["name"], person?.parents?.mother?.name)
        assertEquals(paulProps["id"], person?.parents?.father?.id)
        assertEquals(paulProps["name"], person?.parents?.father?.name)
        person?.onPress?.let { it() }
        assertTrue(action1Called)
        person?.onChange?.let { it("Test") }
        assertEquals("Test", action2CalledWith?.scope?.closestState("onChange")?.get())
    }
}