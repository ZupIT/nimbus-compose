package br.zup.com.nimbus.compose.internal.util

import br.com.zup.nimbus.compose.model.Page

fun Page.observe(): PageStateObserver {
    val observer = PageStateObserver()
    this.testOnChange {
        observer.change(it)
    }
    return observer
}
