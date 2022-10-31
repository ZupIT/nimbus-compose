package br.com.zup.nimbus.compose.sample.model

import br.com.zup.nimbus.annotation.Root

class Size(
    @Root val fixed: FixedDimensions?,
    @Root val min: MinimumDimensions?,
    @Root val max: MaximumDimensions?,
)
