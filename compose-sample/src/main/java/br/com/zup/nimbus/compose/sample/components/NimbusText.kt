package br.com.zup.nimbus.compose.sample.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import br.com.zup.nimbus.compose.sample.model.AdaptiveSize
import br.com.zup.nimbus.compose.sample.model.Address
import br.com.zup.nimbus.compose.sample.model.Person
import br.com.zup.nimbus.compose.sample.model.Sex
import com.zup.nimbus.core.deserialization.AnyServerDrivenData
import com.zup.nimbus.processor.annotation.Alias
import com.zup.nimbus.processor.annotation.AutoDeserialize
import com.zup.nimbus.processor.annotation.Deserializer
import com.zup.nimbus.processor.annotation.Root

@Composable
@AutoDeserialize
fun NimbusText(text: String) {
    Text(text = text)
}

enum class Test {
    TestA, TestB, TestC
}

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
    // Auto deserialized
    person: Person?,
    @Root address: Address,
) {
    Text(text = text)
}