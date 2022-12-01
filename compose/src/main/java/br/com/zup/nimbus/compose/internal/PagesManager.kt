package br.com.zup.nimbus.compose.internal

import br.com.zup.nimbus.compose.CoroutineDispatcherLib
import br.com.zup.nimbus.compose.model.Page
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class PagesManager {
    private val pages = ArrayList<Page>()

    fun add(page: Page) = pages.add(page)

    fun popLastPage(): Boolean {
        if (pages.size <= 1) {
            return false
        }
        this.removeLastPage()
        return true
    }

    fun removeAllPages() {
        pages.clear()
    }

    private fun removeLastPage() = CoroutineScope(CoroutineDispatcherLib.backgroundPool).launch {
        pages.removeLast()
    }

    fun getPageCount() = pages.size
    fun getPageBy(url: String): Page? = pages.firstOrNull { it.id == url }
    fun removePagesAfter(page: Page) = page.removePagesAfter(pages)
}
