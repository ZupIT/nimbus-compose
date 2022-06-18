package br.zup.com.nimbus.compose.core.ui.internal

import br.zup.com.nimbus.compose.CoroutineDispatcherLib
import br.zup.com.nimbus.compose.model.Page
import br.zup.com.nimbus.compose.model.removeAllPages
import br.zup.com.nimbus.compose.model.removeLastPage
import br.zup.com.nimbus.compose.model.removePagesAfter
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
        pages.removeAllPages()
    }

    private fun removeLastPage() =
        CoroutineScope(CoroutineDispatcherLib.backgroundPool).launch {
            pages.removeLastPage()
        }

    fun getPageCount() = pages.size
    fun getPageBy(url: String): Page? = pages.firstOrNull { it.id == url }
    fun findPage(url: String): Page? = pages.firstOrNull {
        it.id == url
    }

    fun removePagesAfter(page: Page) = page.removePagesAfter(pages)

}
