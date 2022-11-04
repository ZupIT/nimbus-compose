package test.utils

object Snippets {
    const val document = """
        enum class DocumentType {
          CPF,
          RG,
          CNH,
        }
        
        data class Document(
          val type: DocumentType,
          val value: String,
        )
    """

    const val myDate = """
        class MyDate {
            var value: Long = 0
            override fun equals(other: Any?) = other is MyDate && value == other.value
            override fun toString() = "${'$'}value"
        }
        
        @Deserializer
        fun deserializeMyDate(data: AnyServerDrivenData): MyDate {
          val result = MyDate()
          result.value = data.asLongOrNull() ?: 0
          return result
        }
    """
}
