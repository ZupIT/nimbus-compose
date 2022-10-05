package br.zup.com.nimbus.compose.internal.deserialization

import br.zup.com.nimbus.compose.deserialization.NimbusDeserializer
import br.zup.com.nimbus.compose.deserialization.annotation.Root
import com.zup.nimbus.core.Nimbus
import com.zup.nimbus.core.ServerDrivenConfig
import com.zup.nimbus.core.deserialization.AnyServerDrivenData
import org.junit.Assert.assertEquals
import org.junit.Test

class Person {
    var id: String = ""
    var name: String = ""
    var birthDate: Int? = null
    var documents: List<Document>? = null
    @Root var parents: ParentsData? = null
}

class ParentsData(
    val mother: Person?,
    val father: Person?,
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
)

class GenericClassDeserializerTest {
    @Test
    fun test() {
        val scope = Nimbus(ServerDrivenConfig(baseUrl = "", platform = "test"))
        val data = AnyServerDrivenData(johnProps)
        val person = NimbusDeserializer.deserialize(data, scope, Person::class)
        data.errors.forEach { println("> $it") }
        assertEquals(0, data.errors.size)
        assertEquals(johnProps["id"], person?.id)
        assertEquals(johnProps["name"], person?.name)
        assertEquals(johnProps["birthDate"], person?.birthDate)
        assertEquals(2, person?.documents?.size)
        assertEquals("ssn", person?.documents?.get(0)?.name)
        assertEquals("47855110", person?.documents?.get(0)?.value)
        assertEquals("drivers license", person?.documents?.get(1)?.name)
        assertEquals("11100023", person?.documents?.get(1)?.value)
        assertEquals(johannaProps["id"], person?.parents?.mother?.id)
        assertEquals(johannaProps["name"], person?.parents?.mother?.name)
        assertEquals(paulProps["id"], person?.parents?.father?.id)
        assertEquals(paulProps["name"], person?.parents?.father?.name)
    }
}