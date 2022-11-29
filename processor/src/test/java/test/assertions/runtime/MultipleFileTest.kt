package test.assertions.runtime

import androidx.compose.material.printedByTextComponent
import androidx.compose.ui.graphics.Color
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import test.BaseRuntimeTest
import test.utils.Snippets
import test.compiler.TestCompiler
import java.util.Date
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("When we create multiple files with deserialization capability")
class MultipleFileTest: BaseRuntimeTest() {
    private val component = "br/com/myApp/components/component.kt" to """
        package br.com.myApp.components
        
        import br.com.zup.nimbus.annotation.AutoDeserialize
        import androidx.compose.runtime.Composable
        import androidx.compose.material.Text
        import br.com.myApp.model.Person

        @AutoDeserialize
        @Composable
        fun Component(person: Person) {
            Text(person.name)
        }
    """

    private val action = "br/com/myApp/actions/action.kt" to """
        package br.com.myApp.actions
        
        import br.com.zup.nimbus.annotation.AutoDeserialize
        import br.com.myApp.model.Person
        import test.TestResult 

        @AutoDeserialize
        fun actionHandler(person: Person) {
            TestResult.add(person)
        }
    """

    private val operation = "br/com/myApp/operations/operation.kt" to """
        package br.com.myApp.operations
        
        import br.com.zup.nimbus.annotation.AutoDeserialize
        import br.com.myApp.model.Person

        @AutoDeserialize
        fun operation(person: Person) = person
    """

    private val person = "br/com/myApp/model/Person.kt" to """
        package br.com.myApp.model

        import java.util.Date

        data class Person(
            val name: String,
            val documents: List<Document>,
            val gender: Gender?,
            val birthDate: Date?,
        )
    """

    private val document = "br/com/myApp/model/Document.kt" to """
        package br.com.myApp.model

        ${Snippets.document}
    """

    private val documentType = "br/com/myApp/model/DocumentType.kt" to """
        package br.com.myApp.model

        ${Snippets.documentType}
    """

    private val gender = "br/com/myApp/model/Gender.kt" to """
        package br.com.myApp.model

        enum class Gender { Male, Female, Other }
    """

    private val dateDeserializer = "br/com/myApp/deserialization/date.kt" to """
        package br.com.myApp.deserialization

        import br.com.zup.nimbus.annotation.Deserializer
        import com.zup.nimbus.core.deserialization.AnyServerDrivenData
        import java.util.Date

        @Deserializer
        fun deserializeDate(data: AnyServerDrivenData) = Date(data.asLong())
    """

    @BeforeAll
    fun setup() {
        val sourceMap = mapOf(component, action, operation, person, document, documentType,
            gender, dateDeserializer)
        compilation = TestCompiler.compile(sourceMap)
        compilation.assertOk()
    }

    private fun className(sourceCode: Pair<String, String>) = sourceCode.first
        .replace("/", ".")
        .replace(Regex("""\.kt$"""), "")

    private fun createPerson(properties: Map<String, Any?>): Any {
        val name = properties["name"]
        val documents = (properties["documents"] as? List<Map<String, Any?>>)?.map {
            val type = compilation.loadEnum(className(documentType), it["type"] as String)
            val value = it["value"]
            compilation.instanceOf(className(document), listOf(type, value))
        }
        val gender = (properties["gender"] as? String)?.let {
            compilation.loadEnum(className(gender), it)
        }
        val birthDate = (properties["birthDate"] as? Long)?.let { Date(it) }
        return compilation.instanceOf(
            className(person),
            listOf(name, documents, gender, birthDate),
        )
    }

    private val properties = mapOf(
        "person" to mapOf(
            "name" to "Shevek",
            "documents" to listOf(mapOf(
                "type" to "CPF",
                "value" to "404",
            )),
            "gender" to "Male",
            "birthDate" to 627991200000,
        )
    )

    @Test
    fun `should deserialize component`() {
        compilation.renderComponent(
            functionName = "Component",
            properties = properties,
            sourceFile = component.first,
        )
        assertEquals(listOf("Shevek" to Color.Black), printedByTextComponent)
    }

    @Test
    fun `should deserialize action handler`() {
        compilation.runEventForActionHandler(
            functionName = "actionHandler",
            properties = properties,
            sourceFile = action.first,
        )
        compilation.assertResults(createPerson(properties["person"] as Map<String, Any?>))
    }

    @Test
    fun `should deserialize operation`() {
        val result = compilation.runOperation(
            functionName = "operation",
            arguments = listOf(properties["person"]),
            sourceFile = operation.first,
        )
        assertEquals(createPerson(properties["person"] as Map<String, Any?>), result)
    }
}
