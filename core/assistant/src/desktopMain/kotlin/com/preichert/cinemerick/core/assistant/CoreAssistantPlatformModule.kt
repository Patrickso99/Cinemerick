package com.preichert.cinemerick.core.assistant

import co.touchlab.kermit.Logger
import com.preichert.cinemerick.core.domain.assistant.ApiKeyLocalDataSource
import com.preichert.cinemerick.core.domain.assistant.CloudProvider
import com.preichert.cinemerick.core.domain.assistant.SpeechError
import com.preichert.cinemerick.core.domain.assistant.SpeechEvent
import com.preichert.cinemerick.core.domain.assistant.SpeechRecognizer
import com.preichert.cinemerick.core.domain.assistant.SystemAiAvailability
import com.preichert.cinemerick.core.domain.assistant.SystemAiDataSource
import com.preichert.cinemerick.core.domain.assistant.SystemAiDownload
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.core.domain.assistant.AssistantError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.koin.dsl.module
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions
import java.util.Properties

actual val coreAssistantPlatformModule = module {
    single<ApiKeyLocalDataSource> { FileApiKeyDataSource() }
    single<SpeechRecognizer> { UnavailableSpeechRecognizer }
    single<SystemAiDataSource> { UnavailableSystemAi }
}

/** Windows, macOS and Linux have no AI of the system the app can call (macOS's is Swift-only and not bridged to the JVM). */
object UnavailableSystemAi : SystemAiDataSource {
    override val name: String? = null
    override suspend fun availability(): SystemAiAvailability = SystemAiAvailability.UNSUPPORTED
    override fun download(): Flow<SystemAiDownload> = emptyFlow()
    override suspend fun complete(system: String, user: String): Result<String, AssistantError> = Result.Failure(AssistantError.NO_ENGINE)
}

/** Desktop has no speech recognition the app can use: the voice sheet opens for typing. */
object UnavailableSpeechRecognizer : SpeechRecognizer {
    override val isAvailable: Boolean = false
    override fun listen(languageTag: String): Flow<SpeechEvent> = flowOf(SpeechEvent.Failed(SpeechError.UNAVAILABLE))
}

private val log = Logger.withTag("FileApiKeyDataSource")

/**
 * Provider keys in a file in the user's home that only the user can read (where the file system has permissions).
 * The JVM has no system keychain shared by Windows, macOS and Linux.
 */
class FileApiKeyDataSource(
    private val file: File = File(System.getProperty("user.home"), ".cinemerick/assistant-keys.properties"),
) : ApiKeyLocalDataSource {

    override fun get(provider: CloudProvider): String? = load().getProperty(provider.name)

    override fun set(provider: CloudProvider, key: String) {
        save(load().apply { setProperty(provider.name, key) })
    }

    override fun remove(provider: CloudProvider) {
        save(load().apply { remove(provider.name) })
    }

    private fun load(): Properties = Properties().apply {
        if (file.exists()) file.inputStream().use(::load)
    }

    private fun save(properties: Properties) {
        try {
            file.parentFile?.mkdirs()
            if (!file.exists()) {
                file.createNewFile()
                restrictToOwner()
            }
            file.outputStream().use { properties.store(it, null) }
        } catch (e: IOException) {
            log.w(e) { "Could not save the assistant keys" }
        }
    }

    private fun restrictToOwner() {
        try {
            Files.setPosixFilePermissions(file.toPath(), PosixFilePermissions.fromString("rw-------"))
        } catch (e: UnsupportedOperationException) {
            // Windows: the file stays in the user's profile, which other users cannot read by default.
            file.setReadable(false, false)
            file.setReadable(true, true)
            file.setWritable(true, true)
        }
    }
}
