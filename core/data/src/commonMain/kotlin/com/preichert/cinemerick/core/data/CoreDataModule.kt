package com.preichert.cinemerick.core.data

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

expect val platformCoreDataModule: Module

val coreDataModule = module {
    singleOf(HttpClientFactory::create)
}
