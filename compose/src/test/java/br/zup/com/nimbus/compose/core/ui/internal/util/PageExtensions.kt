package br.zup.com.nimbus.compose.core.ui.internal.util

import br.zup.com.nimbus.compose.model.Page

fun Page.observe(): PageStateObserver {
    val observer = PageStateObserver()
    this.onChange {
        observer.change(it)
    }
    return observer
}
