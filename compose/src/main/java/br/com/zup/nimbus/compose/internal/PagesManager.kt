/*
 * Copyright 2023 ZUP IT SERVICOS EM TECNOLOGIA E INOVACAO SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
