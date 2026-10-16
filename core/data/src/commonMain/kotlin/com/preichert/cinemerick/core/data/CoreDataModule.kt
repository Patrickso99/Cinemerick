package com.preichert.cinemerick.core.data

import com.preichert.cinemerick.core.domain.AnimationPreferences
import com.preichert.cinemerick.core.domain.LanguagePreferences
import com.preichert.cinemerick.core.domain.ThemePreferences
import com.preichert.cinemerick.core.domain.TimeWindowPreferences
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

expect val platformCoreDataModule: Module

val coreDataModule = module {
    singleOf(HttpClientFactory::create)
    single<ThemePreferences> { SettingsThemePreferences(Settings()) }
    single<LanguagePreferences> { SettingsLanguagePreferences(Settings()) }
    single<AnimationPreferences> { SettingsAnimationPreferences(Settings()) }
    single<TimeWindowPreferences> { SettingsTimeWindowPreferences(Settings()) }
}
