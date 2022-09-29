package br.zup.com.nimbus.compose.internal.util

import br.zup.com.nimbus.compose.model.NimbusPageState
import kotlinx.coroutines.CompletableDeferred

class PageStateObserver {
    private var changes = ArrayList<NimbusPageState>()
    private var onChange: (() -> Unit)? = null

    suspend fun awaitStateChanges(numberOfChanges: Int = 1): List<NimbusPageState> {
        val deferred = CompletableDeferred<List<NimbusPageState>>()
        if (changes.size >= numberOfChanges) deferred.complete(changes)
        onChange = {
            if (changes.size == numberOfChanges) deferred.complete(changes)
        }
        return deferred.await()
    }

    fun change(state: NimbusPageState) {
        changes.add(state)
        onChange?.let { it() }
    }

    fun clear() {
        changes = ArrayList()
        onChange = null
    }
}