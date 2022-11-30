package br.com.zup.nimbus.processor.error

import br.br.com.zup.nimbus.processor.error.AutoDeserializationError
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import br.com.zup.nimbus.processor.utils.getSimpleName

internal class RootCycleError(param: KSValueParameter): AutoDeserializationError(
    "You can't have cycles when using the annotation @Root. Cyclic reference found in " +
            "the constructor of ${getSourceClass(param)} at the parameter named " +
            "${param.name?.asString() ?: "unknown"} of type ${param.type.resolve().getSimpleName()}.",
    param,
) {
    companion object {
        fun getSourceClass(param: KSValueParameter): String {
            val parent = param.parent?.parent
            return if (parent is KSClassDeclaration) parent.simpleName.asString() else "unknown"
        }
    }
}
