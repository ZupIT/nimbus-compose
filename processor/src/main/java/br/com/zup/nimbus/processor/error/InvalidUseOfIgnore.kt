package br.com.zup.nimbus.processor.error

import br.br.com.zup.nimbus.processor.error.AutoDeserializationError
import com.google.devtools.ksp.symbol.KSValueParameter

internal class InvalidUseOfIgnore(param: KSValueParameter): AutoDeserializationError(
    "Parameters annotated with @Ignore must have default values.",
    param,
)
