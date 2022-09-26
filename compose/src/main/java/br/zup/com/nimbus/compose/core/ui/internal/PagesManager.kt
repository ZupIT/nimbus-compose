package br.zup.com.nimbus.compose.core.ui.internal

import br.zup.com.nimbus.compose.model.Page

internal class PagesManager {
    private val pages = ArrayList<Page>()

    fun add(page: Page) = pages.add(page)

    fun popLastPage(): Boolean {
        if (pages.size <= 1) {
            return false
        }

        return true
    }

    fun getPageCount() = pages.size
    fun getPageBy(url: String): Page? = pages.firstOrNull { it.id == url }
}
