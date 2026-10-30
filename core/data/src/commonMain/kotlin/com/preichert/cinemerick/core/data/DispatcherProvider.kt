package com.preichert.cinemerick.core.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

// Parsing a whole page with regexes is CPU work. It is injected so tests can run it on a test dispatcher.
interface DispatcherProvider {
    val parsing: CoroutineDispatcher
}

class DefaultDispatcherProvider : DispatcherProvider {
    override val parsing: CoroutineDispatcher = Dispatchers.Default
}
