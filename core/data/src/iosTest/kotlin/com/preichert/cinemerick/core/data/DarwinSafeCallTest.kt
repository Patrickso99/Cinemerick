package com.preichert.cinemerick.core.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.DarwinHttpRequestException
import io.ktor.client.engine.mock.MockEngine
import kotlinx.coroutines.test.runTest
import platform.Foundation.NSError
import platform.Foundation.NSURLErrorBadURL
import platform.Foundation.NSURLErrorCannotConnectToHost
import platform.Foundation.NSURLErrorCannotFindHost
import platform.Foundation.NSURLErrorDNSLookupFailed
import platform.Foundation.NSURLErrorDomain
import platform.Foundation.NSURLErrorNetworkConnectionLost
import platform.Foundation.NSURLErrorNotConnectedToInternet
import platform.Foundation.NSURLErrorTimedOut
import kotlin.test.Test

class DarwinSafeCallTest {

    private suspend fun resultOf(domain: String?, code: Long): Result<*, DataError.Network> {
        val exception = DarwinHttpRequestException(NSError.errorWithDomain(domain, code, null))
        return HttpClient(MockEngine { throw exception }).getResponse("https://cinema.test")
    }

    @Test
    fun connectivityErrorsAreNoInternet() = runTest {
        val codes = listOf(
            NSURLErrorNotConnectedToInternet,
            NSURLErrorNetworkConnectionLost,
            NSURLErrorCannotFindHost,
            NSURLErrorDNSLookupFailed,
            NSURLErrorCannotConnectToHost,
        )

        codes.forEach { code ->
            assertThat(resultOf(NSURLErrorDomain, code), name = "code=$code")
                .isEqualTo(Result.Failure(DataError.Network.NO_INTERNET))
        }
    }

    @Test
    fun aTimedOutRequestIsARequestTimeout() = runTest {
        assertThat(resultOf(NSURLErrorDomain, NSURLErrorTimedOut))
            .isEqualTo(Result.Failure(DataError.Network.REQUEST_TIMEOUT))
    }

    @Test
    fun anotherUrlErrorIsUnknown() = runTest {
        assertThat(resultOf(NSURLErrorDomain, NSURLErrorBadURL))
            .isEqualTo(Result.Failure(DataError.Network.UNKNOWN))
    }

    @Test
    fun aConnectivityCodeFromAnotherDomainIsUnknown() = runTest {
        assertThat(resultOf("NSPOSIXErrorDomain", NSURLErrorNotConnectedToInternet))
            .isEqualTo(Result.Failure(DataError.Network.UNKNOWN))
    }
}
