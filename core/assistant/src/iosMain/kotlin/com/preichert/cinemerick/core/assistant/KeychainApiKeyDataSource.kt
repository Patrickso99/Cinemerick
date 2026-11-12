package com.preichert.cinemerick.core.assistant

import com.preichert.cinemerick.core.domain.assistant.ApiKeyLocalDataSource
import com.preichert.cinemerick.core.domain.assistant.CloudProvider
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

private const val SERVICE = "com.preichert.cinemerick.assistant"

/** Provider keys in the iOS Keychain, readable only on this device once it has been unlocked. */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class KeychainApiKeyDataSource : ApiKeyLocalDataSource {

    override fun get(provider: CloudProvider): String? = memScoped {
        val query = query(provider) { dict ->
            CFDictionaryAddValue(dict, kSecReturnData, kCFBooleanTrue)
            CFDictionaryAddValue(dict, kSecMatchLimit, kSecMatchLimitOne)
        }
        val result = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(query.dict, result.ptr)
        query.release()
        if (status != errSecSuccess) return@memScoped null
        val data = CFBridgingRelease(result.value) as? NSData ?: return@memScoped null
        NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
    }

    // The Keychain has no upsert: the old item is removed first.
    override fun set(provider: CloudProvider, key: String) {
        remove(provider)
        val data = NSString.create(string = key).dataUsingEncoding(NSUTF8StringEncoding) ?: return
        val dataRef = CFBridgingRetain(data)
        val query = query(provider) { dict ->
            CFDictionaryAddValue(dict, kSecValueData, dataRef)
            CFDictionaryAddValue(dict, kSecAttrAccessible, kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly)
        }
        SecItemAdd(query.dict, null)
        query.release()
        CFRelease(dataRef)
    }

    override fun remove(provider: CloudProvider) {
        val query = query(provider) {}
        SecItemDelete(query.dict)
        query.release()
    }

    private class Query(val dict: CFMutableDictionaryRef?, private val retained: List<CFTypeRef?>) {
        fun release() {
            retained.forEach { CFRelease(it) }
            CFRelease(dict)
        }
    }

    private fun query(provider: CloudProvider, extra: (CFMutableDictionaryRef?) -> Unit): Query {
        val dict = CFDictionaryCreateMutable(null, 0, kCFTypeDictionaryKeyCallBacks.ptr, kCFTypeDictionaryValueCallBacks.ptr)
        val service = CFBridgingRetain(NSString.create(string = SERVICE))
        val account = CFBridgingRetain(NSString.create(string = provider.name))
        CFDictionaryAddValue(dict, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(dict, kSecAttrService, service)
        CFDictionaryAddValue(dict, kSecAttrAccount, account)
        extra(dict)
        return Query(dict, listOf(service, account))
    }
}
