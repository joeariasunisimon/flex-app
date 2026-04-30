package co.jarias.flexapp.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class FlowWatcher<T : Any>(
    private val flow: Flow<T>
) {
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    fun watch(onEach: (T) -> Unit) {
        job = flow.onEach { onEach(it) }.launchIn(scope)
    }

    fun close() {
        job?.cancel()
        job = null
    }
}
