package com.preichert.cinemerick.core.presentation

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import kotlin.time.Clock

val corePresentationModule = module {
    single<Clock> { Clock.System }
    singleOf(::ThemeController)
    singleOf(::AnimationController)
    singleOf(::TimeWindowController)
    singleOf(::LanguageController)
}