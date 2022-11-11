package test.assertions.runtime.annotation

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import test.BaseRuntimeTest
import test.compiler.TestCompiler

@DisplayName("When we use @Root on a parameter")
class RootTest: BaseRuntimeTest() {
    @BeforeAll
    fun setup() {
        compilation = TestCompiler.compile(
            """
                import br.com.zup.nimbus.annotation.Root
                import br.com.zup.nimbus.annotation.Ignore
                import br.com.zup.nimbus.annotation.Alias
                
                class Person(
                    val name: String,
                    val age: Int,
                    val birthDate: Long,
                )
                
                data class Margin(
                    val marginTop: Double?,
                    val marginBottom: Double?,
                )
                
                data class Padding(
                    val paddingTop: Double,
                    val paddingBottom: Double,
                )
                
                data class Spacing(
                    @Root val margin: Margin,
                    @Root val padding: Padding?,
                )
                
                data class Style(
                    @Alias("background") val backgroundColor: String,
                    @Root val spacing: Spacing,
                )
                
                data class ProductDetails(
                  val name: String,
                  val image: String,
                  val description: String?,
                )
                
                data class Product(
                  val id: String,
                  @Root val details: ProductDetails,
                )
                
                @AutoDeserialize
                fun oneLevelRootAction(@Root person: Person) {
                    TestResult.add(person.name, person.age, person.birthDate)
                }
                
                @AutoDeserialize
                fun multiLevelRootAction(@Root style: Style?) {
                    TestResult.add(style)
                }
                
                @AutoDeserialize
                fun multiLevelRequiredRootAction(@Root product: Product) {
                    TestResult.add(product)
                }
                
                @AutoDeserialize
                fun clashingNamesRootAction(
                    @Root person: Person,
                    name: Int,
                    age: String,
                ) {
                    TestResult.add(
                        person.name,
                        person.age,
                        person.birthDate,
                        name,
                        age,
                    )
                }
                
                @AutoDeserialize
                fun ignoredRootAction(
                    @Ignore @Root person: Person? = null,
                    name: String,
                ) {
                    TestResult.add(person, name)
                }
                
                @AutoDeserialize
                fun aliasedRootAction(@Alias("test") @Root person: Person? = null) {
                    TestResult.add(person?.name, person?.age, person?.birthDate)
                }
            """,
        )
    }

    private fun createMargin(top: Double?, bottom: Double?) =
        compilation.instanceOf("Margin", listOf(top, bottom))

    private fun createPadding(top: Double, bottom: Double) =
        compilation.instanceOf("Padding", listOf(top, bottom))

    private fun createSpacing(margin: Any, padding: Any?) =
        compilation.instanceOf("Spacing",listOf(margin, padding))

    private fun createStyle(background: String, spacing: Any) =
        compilation.instanceOf("Style", listOf(background, spacing))

    private fun createStyle(properties: Map<String, Any>): Any {
        val margin = createMargin(
            properties["marginTop"] as? Double,
            properties["marginBottom"] as? Double,
        )
        val padding = createPadding(
            properties["paddingTop"] as Double,
            properties["paddingBottom"] as Double,
        )
        val spacing = createSpacing(margin, padding)
        return createStyle(properties["background"] as String, spacing)
    }

    private fun shouldDeserializeRootProperty(functionName: String) {
        val properties = mapOf(
            "name" to "Joseph",
            "age" to 32,
            "birthDate" to 10L,
        )
        compilation.runEventForActionHandler(functionName, properties)
        compilation.assertResults("Joseph", 32, 10L)
    }

    @Test
    @DisplayName("Then it should look for parameters at the root level of the map")
    fun `should look for parameters at the root level`() =
        shouldDeserializeRootProperty("oneLevelRootAction")

    @Test
    @DisplayName("When we use @Root in conjunction with @Ignore, the @Root should have no effect")
    fun `@Root should have no effect`() {
        val properties = mapOf("name" to "Joseph")
        compilation.runEventForActionHandler("ignoredRootAction", properties)
        compilation.assertResults(null, "Joseph")
    }

    @Test
    @DisplayName("When we use @Root in conjunction with @Alias, the @Alias should have no effect")
    fun `@Alias should have no effect`() = shouldDeserializeRootProperty("aliasedRootAction")

    @Test
    @DisplayName("When we use @Root and some properties end up with name clashes, the clashing " +
            "parameters should have the same value source")
    fun `clashing parameters should get the same value`() {
        val properties = mapOf(
            "name" to "325.45",
            "age" to 32,
            "birthDate" to 10L,
        )
        compilation.runEventForActionHandler("clashingNamesRootAction", properties)
        compilation.assertResults("325.45", 32, 10L, 325, "32")
    }

    @Nested
    @DisplayName("When we use nested @Root annotations")
    inner class NestedRoot {
        @Test
        @DisplayName("Then it should look for all parameter at the root level of the map")
        fun `should look for all parameter at the root level`() {
            val properties = mapOf(
                "background" to "#FFF",
                "marginTop" to 10.0,
                "marginBottom" to 15.0,
                "paddingTop" to 20.0,
                "paddingBottom" to 25.0,
            )
            compilation.runEventForActionHandler("multiLevelRootAction", properties)
            compilation.assertResults(createStyle(properties))
        }

        @Test
        @DisplayName("When we don't provide any of the properties of an optional root parameter," +
                "Then it should be deserialized as null")
        fun `should be deserialized as null`() {
            // no style (style is optional)
            compilation.runEventForActionHandler("multiLevelRootAction", emptyMap())
            compilation.assertResults(null)
            compilation.clearResults()
            // no padding (padding is optional)
            val noPadding = mapOf(
                "background" to "#FFF",
                "marginTop" to 10,
                "marginBottom" to 15,
            )
            compilation.runEventForActionHandler("multiLevelRootAction", noPadding)
            val margin = createMargin(10.0, 15.0)
            val spacing = createSpacing(margin, null)
            compilation.assertResults(createStyle("#FFF", spacing))
        }

        @Test
        @DisplayName("When we provide some but not all required properties of an optional root " +
                "parameter, Then it should fail to deserialize")
        fun `should fail to deserialize optional root parameter`() {
            // padding is optional and paddingBottom is required
            val wrongPadding = mapOf(
                "background" to "#FFF",
                "marginTop" to 10,
                "marginBottom" to 15,
                "paddingTop" to 20,
            )
            val errors = listOf("Expected a number for property \"paddingBottom\", but found null")
            compilation.runEventForActionHandlerCatching("multiLevelRootAction", wrongPadding, errors)
        }

        @Test
        @DisplayName("When we don't provide any of the properties of a required root parameter " +
                "that has only optional properties, Then it should be deserialized as an empty " +
                "object")
        fun `should be deserialized as an empty object`() {
            // margin is required, but all of its properties are optional
            val properties = mapOf(
                "background" to "#FFF",
                "paddingTop" to 10,
                "paddingBottom" to 15,
            )
            compilation.runEventForActionHandler("multiLevelRootAction", properties)
            val margin = createMargin(null, null)
            val padding = createPadding(10.0, 15.0)
            val spacing = createSpacing(margin, padding)
            compilation.assertResults(createStyle("#FFF", spacing))
        }

        @Test
        @DisplayName("When we don't provide any of the properties of a required root parameter " +
                "that ends up requiring no property via the @Root annotation, Then it should " +
                "deserialize with nulls and empty objects")
        fun `should deserialize required parameter that ends up requiring nothing`() {
            // spacing.margin.marginTop, spacing.margin.marginBottom and spacing.padding are all
            // optional, even though spacing is not.
            val properties = mapOf("background" to "#FFF")
            compilation.runEventForActionHandler("multiLevelRootAction", properties)
            val margin = createMargin(null, null)
            val spacing = createSpacing(margin, null)
            val style = createStyle("#FFF", spacing)
            compilation.assertResults(style)
        }

        @Test
        @DisplayName("When we don't provide any of the properties of a required root parameter " +
                "that ends up having required properties, Then it should fail to deserialize")
        fun `should fail to deserialize required root parameter`() {
            // both product and product.details are required
            val properties = mapOf("id" to "1")
            val errors = listOf(
                "Expected a string for property \"name\", but found null",
                "Expected a string for property \"image\", but found null",
            )
            compilation.runEventForActionHandlerCatching(
                "multiLevelRequiredRootAction", properties, errors,
            )
        }
    }
}
