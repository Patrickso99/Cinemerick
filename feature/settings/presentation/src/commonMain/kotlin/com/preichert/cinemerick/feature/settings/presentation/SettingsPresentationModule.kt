package com.preichert.cinemerick.feature.settings.presentation

import com.preichert.cinemerick.feature.settings.presentation.key.AssistantKeyViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingsPresentationModule = module {
    viewModelOf(::SettingsViewModel)
    viewModelOf(::AssistantKeyViewModel)
}