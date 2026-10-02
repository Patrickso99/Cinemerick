package com.preichert.cinemerick.core.data

import com.preichert.cinemerick.core.domain.ThemePreferences
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformCoreDataModule: Module

val coreDataModule = module {
    single { HttpClientFactory.create(get()) }
    single<ThemePreferences> { SettingsThemePreferences(Settings()) }
}
