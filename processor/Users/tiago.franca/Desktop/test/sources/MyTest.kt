    import test.TestResult
    import br.com.zup.nimbus.annotation.AutoDeserialize
    import br.com.zup.nimbus.annotation.Deserializer
    import com.zup.nimbus.core.deserialization.AnyServerDrivenData
    import br.zup.com.nimbus.compose.deserialization.DeserializationContext
    import androidx.compose.runtime.Composable

    
        
class MyDate {
    var value: Long = 0
    override fun equals(other: Any?) = other is MyDate && value == other.value
    override fun toString() = "$value"
}

@Deserializer
fun deserializeMyDate(data: AnyServerDrivenData): MyDate {
  val result = MyDate()
  result.value = data.asLongOrNull() ?: 0
  return result
}

        
enum class DocumentType {
  CPF,
  RG,
  CNH,
}

data class Document(
  val type: DocumentType,
  val value: String,
)

        
        class MyComponentData(
          val context: DeserializationContext,
          val content: @Composable () -> Unit,
        )
        
        @AutoDeserialize
        @Composable
        fun LeafComponent(
            name: String?,
            date: MyDate,
            metadata: Any,
            age: Int?,
            onPress: () -> Unit,
            documents: List<Document>,
        ) {
            onPress()
            TestResult.add(name, date, metadata, age, documents)
        }
        
        @AutoDeserialize
        @Composable
        fun ContainerComponent(content: @Composable () -> Unit) {
            content()
        }
        
        @AutoDeserialize
        @Composable
        fun ContextAwareComponent(context: DeserializationContext) {
            TestResult.add(context.component?.node?.id)
        }
        
        @AutoDeserialize
        @Composable
        fun ClassPropsComponent(data: MyComponentData) {
            data.content()
            TestResult.add(data.context.component?.node?.id, data.context.event?.scope?.name)
        }
    