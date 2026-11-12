package com.preichert.cinemerick.core.assistant

import com.preichert.cinemerick.core.domain.assistant.AssistantPreferences
import com.preichert.cinemerick.core.domain.assistant.AssistantRepository
import com.preichert.cinemerick.core.domain.assistant.CloudLlmRemoteDataSource
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/** Provides what differs by platform: the secure key store, the speech recognizer and the AI of the system. */
expect val coreAssistantPlatformModule: Module

val coreAssistantModule = module {
    includes(coreAssistantPlatformModule)

    singleOf(::SettingsAssistantPreferences) { bind<AssistantPreferences>() }
    singleOf(::KtorCloudLlmDataSource) { bind<CloudLlmRemoteDataSource>() }
    single<AssistantRepository> { EngineSwitchingAssistantRepository(get(), get(), get(), get()) }
}
