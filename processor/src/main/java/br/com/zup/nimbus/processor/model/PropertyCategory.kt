package br.com.zup.nimbus.processor.model

internal enum class PropertyCategory {
    /**
     * Parameter annotated with @Root
     */
    Root,
    /**
     * Parameter of type `DeserializationContext`
     */
    Context,
    /**
     * Parameter type annotated with @Composable
     */
    Composable,
    /**
     * None of the above
     */
    Common,
}
