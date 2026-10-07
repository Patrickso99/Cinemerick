package com.preichert.cinemerick.core.presentation

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val corePresentationModule = module {
    singleOf(::ThemeController)
}