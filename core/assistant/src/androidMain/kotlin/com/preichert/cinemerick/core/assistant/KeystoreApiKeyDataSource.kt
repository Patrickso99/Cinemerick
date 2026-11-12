package com.preichert.cinemerick.core.assistant

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import co.touchlab.kermit.Logger
import com.preichert.cinemerick.core.domain.assistant.ApiKeyLocalDataSource
import com.preichert.cinemerick.core.domain.assistant.CloudProvider
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private val log = Logger.withTag("KeystoreApiKeyDataSource")

private const val KEYSTORE = "AndroidKeyStore"
private const val KEY_ALIAS = "cinemerick_assistant_keys"
private const val TRANSFORMATION = "AES/GCM/NoPadding"
private const val PREFS_NAME = "assistant_keys"
private const val TAG_BITS = 128
private const val SEPARATOR = ":"

/**
 * Provider keys encrypted with an AES key that never leaves the Android Keystore. Only the ciphertext is stored, in a
 * private preferences file. A key that cannot be decrypted (e.g. restored on another device) reads as missing.
 */
class KeystoreApiKeyDataSource(context: Context) : ApiKeyLocalDataSource {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun get(provider: CloudProvider): String? {
        val stored = prefs.getString(provider.name, null) ?: return null
        return try {
            val (iv, cipherText) = stored.split(SEPARATOR).map { Base64.decode(it, Base64.NO_WRAP) }
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(TAG_BITS, iv))
            cipher.doFinal(cipherText).decodeToString()
        } catch (e: Exception) {
            log.w(e) { "Could not decrypt the ${provider.name} key" }
            null
        }
    }

    override fun set(provider: CloudProvider, key: String) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val cipherText = cipher.doFinal(key.encodeToByteArray())
        val stored = Base64.encodeToString(cipher.iv, Base64.NO_WRAP) + SEPARATOR + Base64.encodeToString(cipherText, Base64.NO_WRAP)
        prefs.edit().putString(provider.name, stored).apply()
    }

    override fun remove(provider: CloudProvider) {
        prefs.edit().remove(provider.name).apply()
    }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return generator.generateKey()
    }
}
