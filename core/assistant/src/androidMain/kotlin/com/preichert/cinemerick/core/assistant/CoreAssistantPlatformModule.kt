package com.preichert.cinemerick.core.assistant

import com.preichert.cinemerick.core.domain.assistant.ApiKeyLocalDataSource
import com.preichert.cinemerick.core.domain.assistant.SpeechRecognizer
import com.preichert.cinemerick.core.domain.assistant.SystemAiDataSource
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val coreAssistantPlatformModule = module {
    single<ApiKeyLocalDataSource> { KeystoreApiKeyDataSource(androidContext()) }
    single<SpeechRecognizer> { AndroidSpeechRecognizer(androidContext()) }
    single<SystemAiDataSource> { GeminiNanoDataSource() }
}
