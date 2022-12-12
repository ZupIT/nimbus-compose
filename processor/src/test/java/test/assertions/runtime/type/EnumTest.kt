/*
 * Copyright 2023 ZUP IT SERVICOS EM TECNOLOGIA E INOVACAO SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test.assertions.runtime.type

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import test.BaseRuntimeTest
import test.compiler.TestCompiler

@DisplayName("When action handlers with enum types are deserialized")
class EnumTest: BaseRuntimeTest() {
    @BeforeAll
    fun setup() {
        compilation = TestCompiler.compile(
            """
                enum class ProductType {
                  Tv,
                  Monitor,
                  Computer,
                  Hardware,
                  Accessories,
                  Software,
                }
                
                enum class ClientPriority {
                  Unsubscribed,
                  Basic,
                  FrequentBuyer,
                  Premium,
                }
                
                @AutoDeserialize
                fun enumAction(
                  product: ProductType,
                  client: ClientPriority?,
                ) {
                    TestResult.add(product, client)
                }
            """,
        )
        compilation.assertOk()
    }

    @DisplayName("When both properties are given with the same case as the original enums, it " +
            "should deserialize.")
    @Test
    fun `should deserialize both properties`() {
        val properties = mapOf(
            "product" to "Tv",
            "client" to "Unsubscribed",
        )
        val tv = compilation.loadEnum("ProductType", "Tv")
        val client = compilation.loadEnum("ClientPriority", "Unsubscribed")
        compilation.runEventForActionHandler("enumAction", properties)
        compilation.assertResults(tv, client)
    }

    @DisplayName("When only the required property is given, it should deserialize.")
    @Test
    fun `should deserialize required property`() {
        val properties = mapOf("product" to "Monitor")
        val monitor = compilation.loadEnum("ProductType", "Monitor")
        compilation.runEventForActionHandler("enumAction", properties)
        compilation.assertResults(monitor, null)
    }

    @DisplayName("When the required property is missing, it should fail.")
    @Test
    fun `should fail when only required property is missing`() {
        val properties = mapOf("client" to "Unsubscribed")
        val errors = listOf("Expected Tv, Monitor, Computer, Hardware, Accessories, Software " +
                "for property \"product\", but found null")
        compilation.runEventForActionHandlerCatching("enumAction", properties, errors)
    }

    @DisplayName("When the required property is invalid, it should fail.")
    @Test
    fun `should fail when required property is invalid`() {
        val properties = mapOf(
            "client" to "Unsubscribed",
            "product" to "Clothing"
        )
        val errors = listOf("Expected Tv, Monitor, Computer, Hardware, Accessories, Software " +
                "for property \"product\", but found Clothing")
        compilation.runEventForActionHandlerCatching("enumAction", properties, errors)
    }

    @DisplayName("When the optional property is invalid, it should fail.")
    @Test
    fun `should deserialize when required property is given and optional is invalid`() {
        val properties = mapOf(
            "client" to "Diamond",
            "product" to "Computer"
        )
        val errors = listOf(
            "Expected Unsubscribed, Basic, FrequentBuyer, Premium for property \"client\", but found Diamond",
        )
        compilation.runEventForActionHandlerCatching("enumAction", properties, errors)
    }

    @DisplayName("When both properties are given, but with different casing, it should deserialize.")
    @Test
    fun `should deserialize when properties have different cases`() {
        val properties = mapOf(
            "client" to "FRequEnTBUYeR",
            "product" to "accESsORiEs"
        )
        val frequentBuyer = compilation.loadEnum("ClientPriority", "FrequentBuyer")
        val accessories = compilation.loadEnum("ProductType", "Accessories")
        compilation.runEventForActionHandler("enumAction", properties)
        compilation.assertResults(accessories, frequentBuyer)
    }
}
