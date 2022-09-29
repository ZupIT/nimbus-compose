package br.zup.com.nimbus.compose.internal.util

import br.zup.com.nimbus.compose.model.Page

fun Page.observe(): PageStateObserver {
    val observer = PageStateObserver()
    this.testOnChange {
        observer.change(it)
    }
    return observer
}
