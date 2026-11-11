package com.preichert.cinemerick.core.data

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.dsl.module

actual val coreDataPlatformModule = module {
    single<HttpClientEngine> { Darwin.create() }
}
