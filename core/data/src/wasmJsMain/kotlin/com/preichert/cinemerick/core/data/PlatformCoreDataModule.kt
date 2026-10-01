package com.preichert.cinemerick.core.data

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js
import org.koin.dsl.module

actual val platformCoreDataModule = module {
    single<HttpClientEngine> { Js.create() }
}
