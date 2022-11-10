package test.assertions.runtime.type

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import test.BaseTest
import test.compiler.CompilationResult
import test.utils.Snippets
import test.compiler.TestCompiler
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("When action handlers with auto deserialized types are deserialized")
class AutoDeserializedTest: BaseTest() {
    private val jessica = mapOf(
        "name" to mapOf(
            "title" to "Ms",
            "firstName" to "Jessica",
            "middleName" to "Simpson",
            "lastName" to "Oliver",
            "nickName" to "Johny",
        ),
        "gender" to "Female",
        "birth" to mapOf(
            // all numbers are instantiated as Longs here, this is why we convert them to int, it's
            // not redundant!
            "day" to 30.toInt(),
            "month" to 1.toInt(),
            "year" to 1969.toInt(),
            "age" to 53.toInt(),
            "unixTime" to -28976400000L,
        ),
        "mother" to null,
        "balance" to 52.65,
        "documents" to listOf(mapOf(
            "type" to "CPF",
            "value" to "12677399823",
        )),
    )
    private val john = mapOf(
        "name" to mapOf(
            "title" to "Mr",
            "firstName" to "John",
            "middleName" to "Stone",
            "lastName" to "Oliver",
            "nickName" to "Johny",
        ),
        "gender" to "Male",
        "birth" to mapOf(
            "day" to 12.toInt(),
            "month" to 4.toInt(),
            "year" to 1994.toInt(),
            "age" to 28.toInt(),
            "unixTime" to 766162800000L,
        ),
        "mother" to jessica,
        "balance" to 100.45,
        "documents" to listOf(mapOf(
            "type" to "CPF",
            "value" to "08388315623",
        )),
    )
    private val hilary = mapOf(
        "name" to mapOf(
            "firstName" to "Hilary",
            "lastName" to "Silver",
        ),
        "birth" to john["birth"],
        "balance" to 87.32,
    )
    private val joseph = mapOf(
        "name" to mapOf(
            "firstName" to "Joseph",
        ),
        "mother" to mapOf(
            "name" to mapOf(
                "firstName" to "Cynthia",
            ),
        ),
        "balance" to 87.32,
    )
    private val hook = mapOf(
        "name" to "Captain Hook",
        "birth" to mapOf(
            "day" to "first",
            "month" to "january",
            "year" to 1994.toInt(),
            "age" to true,
            "unixTime" to "once upon a time",
        ),
        "balance" to false,
    )

    @BeforeAll
    fun setup(compiler: TestCompiler) {
        compilation = compiler.compile(
            """
                import com.zup.nimbus.core.ServerDrivenState
                
                // Testing nested and recursive auto-deserialized structures
                
                ${Snippets.documentAndDocumentType}
                ${Snippets.myDate}
                
                enum class Gender { Male, Female, Other }
                enum class Title { Mr, Ms, Doctor, Professor, YourHighness }
                
                data class Name(
                  val title: Title?,
                  val firstName: String,
                  val middleName: String?,
                  val lastName: String,
                  val nickName: String?,
                )
                
                data class BirthData(
                  val day: Int,
                  val month: Int,
                  val year: Int,
                  val age: Int,
                  val unixTime: MyDate,
                )
                
                data class Person(
                  val name: Name,
                  val gender: Gender?,
                  val birth: BirthData,
                  val mother: Person?,
                  val balance: Double,
                  val documents: List<Document>?,
                )
                
                // Testing cyclic references
                
                data class A(val aValue: B)
                data class B(val bValue: C?)
                data class C(val cValue: A)
                
                // Action

                @AutoDeserialize
                fun changeName(user: Person, newName: Name?) {
                  TestResult.add(user, newName)
                }
                
                @AutoDeserialize
                fun cyclic(a: A) {
                  TestResult.add(a)
                }
                
                @AutoDeserialize
                fun externalDependency(state: ServerDrivenState) {
                  TestResult.add(state.id, state.get())
                }
            """,
        )
        compilation.assertOk()
    }

    private fun myDate(value: Long) = compilation.instanceOf("MyDate", value)
    
    private fun name(properties: Map<String, Any?>) = compilation.instanceOf(
        "Name",
        listOf(properties["title"]?.let { compilation.loadEnum("Title", it as String) },
            properties["firstName"], properties["middleName"], properties["lastName"],
            properties["nickName"]),
    )

    private fun birthData(properties: Map<String, Any?>) = compilation.instanceOf(
        "BirthData",
        listOf(properties["day"], properties["month"], properties["year"], properties["age"],
            myDate(properties["unixTime"] as Long)),
    )

    private fun document(properties: Map<String, Any?>) = compilation.instanceOf(
        "Document",
        listOf(
            compilation.loadEnum("DocumentType", properties["type"] as String),
            properties["value"],
        )
    )

    private fun person(properties: Map<String, Any?>): Any = compilation.instanceOf(
        "Person",
        listOf(
            properties["name"]?.let { name(it as Map<String, Any?>) },
            properties["gender"]?.let { compilation.loadEnum("Gender", it as String) },
            properties["birth"]?.let { birthData(it as Map<String, Any?>) },
            properties["mother"]?.let { person(it as Map<String, Any?>) },
            properties["balance"],
            (properties["documents"] as? List<Map<String, Any?>>)?.let { list ->
                list.map { document(it) }
            },
        )
    )
        

    @DisplayName("When all properties are given correctly, it should deserialize")
    @Test
    fun `should deserialize all properties if they're given correctly`() {
        val newName = mapOf(
            "title" to "YourHighness",
            "firstName" to "John",
            "middleName" to "Water",
            "lastName" to "Oliver",
            "nickName" to "Oliver",
        )
        val properties = mapOf(
            "user" to john,
            "newName" to newName,
        )
        val personInstance = person(john)
        val nameInstance = name(newName)
        compilation.runEventForActionHandler("changeName", properties)
        compilation.assertResults(personInstance, nameInstance)
    }

    @DisplayName("When some optional properties are missing, it should deserialize.")
    @Test
    fun `should deserialize if some optional properties are missing`() {
        val newName = mapOf(
            "title" to "Ms",
            "firstName" to "Hilary",
            "lastName" to "Silver",
        )
        val properties = mapOf(
            "user" to hilary,
            "newName" to newName,
        )
        val personInstance = person(hilary)
        val nameInstance = name(newName)
        compilation.runEventForActionHandler("changeName", properties)
        compilation.assertResults(personInstance, nameInstance)
    }

    @DisplayName("When some required properties are missing, it should fail.")
    @Test
    fun `should fail to deserialize if some required properties are missing`() {
        val newName = mapOf(
            "lastName" to "Silver",
        )
        val properties = mapOf(
            "user" to joseph,
            "newName" to newName,
        )
        val errors = listOf(
            "Expected a string for property \"user.name.lastName\", but found null",
            "Expected a number for property \"user.birth.day\", but found null",
            "Expected a number for property \"user.birth.month\", but found null",
            "Expected a number for property \"user.birth.year\", but found null",
            "Expected a number for property \"user.birth.age\", but found null",
            "Expected a string for property \"user.mother.name.lastName\", but found null",
            "Expected a number for property \"user.mother.birth.day\", but found null",
            "Expected a number for property \"user.mother.birth.month\", but found null",
            "Expected a number for property \"user.mother.birth.year\", but found null",
            "Expected a number for property \"user.mother.birth.age\", but found null",
            "Expected a number for property \"user.mother.balance\", but found null",
            "Expected a string for property \"newName.firstName\", but found null",
        )
        compilation.runEventForActionHandlerCatching("changeName", properties, errors)
    }

    @DisplayName("When some properties are invalid, it should fail.")
    @Test
    fun `should fail to deserialize if some properties are invalid`() {
        val newName = mapOf(
            "title" to "Captain",
            "firstName" to "Hook",
            "lastName" to "Pirate",
        )
        val properties = mapOf(
            "user" to hook,
            "newName" to newName,
        )
        val errors = listOf(
            "Expected a string for property \"user.name.firstName\", but found null",
            "Expected a string for property \"user.name.lastName\", but found null",
            "Expected a number for property \"user.birth.day\", but found String",
            "Expected a number for property \"user.birth.month\", but found String",
            "Expected a number for property \"user.birth.age\", but found Boolean",
            "Expected a number for property \"user.birth.unixTime\", but found String",
            "Expected a number for property \"user.balance\", but found Boolean",
            "Expected Mr, Ms, Doctor, Professor, YourHighness for property \"newName.title\", but found Captain",
        )
        compilation.runEventForActionHandlerCatching("changeName", properties, errors)
    }

    @DisplayName("When we have a cyclic dependence, it should just work (deserialize)")
    @Test
    fun `should deserialize cyclic dependency`() {
        val properties = mapOf(
            "a" to mapOf(
                "aValue" to mapOf(
                    "bValue" to mapOf(
                        "cValue" to mapOf(
                            "aValue" to mapOf(
                                "bValue" to mapOf(
                                    "cValue" to mapOf(
                                        "aValue" to mapOf(
                                            "bValue" to null
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
        compilation.runEventForActionHandler("cyclic", properties)
        val b3 = compilation.instanceOf("B", listOf(null))
        val a3 = compilation.instanceOf("A", listOf(b3))
        val c2 = compilation.instanceOf("C", listOf(a3))
        val b2 = compilation.instanceOf("B", listOf(c2))
        val a2 = compilation.instanceOf("A", listOf(b2))
        val c1 = compilation.instanceOf("C", listOf(a2))
        val b1 = compilation.instanceOf("B", listOf(c1))
        val a1 = compilation.instanceOf("A", listOf(b1))
        compilation.assertResults(a1)
    }

    @Test
    @DisplayName("When we use the auto-deserialization on an external class (outside the current " +
            "module), it should deserialize")
    fun `should deserialize external class`() {
        val properties = mapOf("state" to mapOf("id" to "myState", "value" to 10))
        compilation.runEventForActionHandler("externalDependency", properties)
        compilation.assertResults("myState", 10)
    }
}
