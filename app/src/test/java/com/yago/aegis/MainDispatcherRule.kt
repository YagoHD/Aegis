package com.yago.aegis

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * US-08: sustituye el dispatcher Main por uno de test para poder construir ViewModels
 * (que usan viewModelScope sobre Dispatchers.Main) en pruebas JVM. Con StandardTestDispatcher
 * sin avanzar, los coroutines lanzados en `init` quedan encolados y no interfieren con las
 * aserciones sobre estado síncrono.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) { Dispatchers.setMain(dispatcher) }
    override fun finished(description: Description) { Dispatchers.resetMain() }
}
