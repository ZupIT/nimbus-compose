package br.com.zup.nimbus.compose.sample.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import br.com.zup.nimbus.compose.sample.model.AdaptiveSize
import br.com.zup.nimbus.compose.sample.model.Address
import br.com.zup.nimbus.compose.sample.model.Person
import br.com.zup.nimbus.compose.sample.model.Sex
import br.com.zup.nimbus.compose.sample.model.Tree
import br.com.zup.nimbus.annotation.Alias
import br.com.zup.nimbus.annotation.AutoDeserialize
import br.com.zup.nimbus.annotation.Ignore
import br.com.zup.nimbus.annotation.Root
import br.zup.com.nimbus.compose.deserialization.DeserializationContext

@Composable
@AutoDeserialize
fun NimbusText(text: String) {
    Text(text = text)
}

enum class Test {
    TestA, TestB, TestC
}

interface TestInterface {
    fun hello(): String
}

typealias TextInputEvent = ((value: String) -> Unit)?

@Composable
@AutoDeserialize
@Suppress("FunctionNaming", "LongParameterList")
fun ComprehensiveTest(
    // primitive, required
    text: String,
    int: Int,
    double: Double,
    long: Long,
    float: Float,
    bool: Boolean,
    enumTest: Test,
    sex: Sex,
    // primitive, nullable
    ntext: String?,
    nint: Int?,
    ndouble: Double?,
    nlong: Long?,
    nfloat: Float?,
    nbool: Boolean?,
    nenumTest: Test?,
    // primitive, alias
    @Alias("text") atext: String,
    @Alias("int") aint: Int,
    @Alias("double") adouble: Double,
    @Alias("long") along: Long,
    @Alias("float") afloat: Float,
    @Alias("bool") abool: Boolean,
    @Alias("enumTest") aenumTest: Test,
    // Lists
    stringList: List<String>?,
    @Alias("aliasedList") intListList: List<List<Int?>>,
    booleanListListList: List<List<List<Boolean>>?>,
    enumList: List<Test?>,
    // Maps
    stringMap: Map<String, String>?,
    @Alias("aliasedMap") intMapMap: Map<String, Map<String, Int?>>,
    booleanMapMapMap: Map<String, Map<String, Map<String, Boolean>>?>,
    // combined lists and maps
    stringMapList: List<Map<String, String>>,
    @Alias("aliasedListMap") intListMap: Map<String, List<Int?>>,
    booleanMapListMapList: List<Map<String, List<Map<String, Boolean>>>?>,
    // Custom deserializer
    size: AdaptiveSize?,
    sizes: List<AdaptiveSize?>?,
    sizeMap: Map<String, AdaptiveSize?>,
    // Auto deserialized
    person: Person?,
    @Root address: Address,
    @Root address2: Address?,
    addresses: List<Address>,
    people: Map<String, Person>?,
    @Ignore ignoredInterface: TestInterface? = null,
    // Functions
    content: @Composable () -> Unit,
    onPress: (() -> Unit)?,
    @Alias("change") onChange: (List<Map<String, Boolean>>) -> Unit,
    // context
    ctx: DeserializationContext,
    // Generic types
    stringTree: Tree<String>,
    intTree: Tree<Int>,
    // Type Aliases (Function)
    onChangeValue: TextInputEvent,
) {
    Text(text = text)
}
