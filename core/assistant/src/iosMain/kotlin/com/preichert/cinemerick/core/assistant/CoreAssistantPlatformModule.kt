package com.preichert.cinemerick.core.assistant

import com.preichert.cinemerick.core.domain.assistant.ApiKeyLocalDataSource
import com.preichert.cinemerick.core.domain.assistant.SpeechRecognizer
import org.koin.dsl.module

actual val coreAssistantPlatformModule = module {
    single<ApiKeyLocalDataSource> { KeychainApiKeyDataSource() }
    single<SpeechRecognizer> { IosSpeechRecognizer() }
}
