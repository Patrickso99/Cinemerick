package com.preichert.cinemerick.core.data

import com.preichert.cinemerick.core.domain.AnimationPreferences
import com.preichert.cinemerick.core.domain.LanguagePreferences
import com.preichert.cinemerick.core.domain.ThemePreferences
import com.preichert.cinemerick.core.domain.TimeWindowPreferences
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import kotlin.time.Clock

expect val coreDataPlatformModule: Module

val coreDataModule = module {
    includes(coreDataPlatformModule)

    single<Clock> { Clock.System }
    single<Settings> { Settings() }
    singleOf(HttpClientFactory::create)

    singleOf(::DefaultDispatcherProvider) { bind<DispatcherProvider>() }
    singleOf(::SettingsThemePreferences) { bind<ThemePreferences>() }
    singleOf(::SettingsLanguagePreferences) { bind<LanguagePreferences>() }
    singleOf(::SettingsAnimationPreferences) { bind<AnimationPreferences>() }
    singleOf(::SettingsTimeWindowPreferences) { bind<TimeWindowPreferences>() }
}
