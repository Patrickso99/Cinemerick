package com.preichert.cinemerick.core.presentation

import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.presentation.resources.Res
import com.preichert.cinemerick.core.presentation.resources.error_access_denied
import com.preichert.cinemerick.core.presentation.resources.error_no_internet
import com.preichert.cinemerick.core.presentation.resources.error_serialization
import com.preichert.cinemerick.core.presentation.resources.error_server
import com.preichert.cinemerick.core.presentation.resources.error_timeout
import com.preichert.cinemerick.core.presentation.resources.error_unknown
import kotlin.test.Test
import kotlin.test.assertEquals

class DataErrorToUiTextTest {

    @Test
    fun connectionProblemsSayWhatHappened() {
        assertEquals(UiText.Resource(Res.string.error_no_internet), DataError.Network.NO_INTERNET.toUiText())
        assertEquals(UiText.Resource(Res.string.error_timeout), DataError.Network.REQUEST_TIMEOUT.toUiText())
    }

    @Test
    fun serverProblemsShareOneMessage() {
        assertEquals(UiText.Resource(Res.string.error_server), DataError.Network.SERVER_ERROR.toUiText())
        assertEquals(UiText.Resource(Res.string.error_server), DataError.Network.SERVICE_UNAVAILABLE.toUiText())
    }

    @Test
    fun refusedAccessSaysSo() {
        assertEquals(UiText.Resource(Res.string.error_access_denied), DataError.Network.UNAUTHORIZED.toUiText())
        assertEquals(UiText.Resource(Res.string.error_access_denied), DataError.Network.FORBIDDEN.toUiText())
    }

    @Test
    fun aBrokenResponseIsReportedAsSerialization() {
        assertEquals(UiText.Resource(Res.string.error_serialization), DataError.Network.SERIALIZATION.toUiText())
    }

    @Test
    fun everythingElseIsUnknown() {
        assertEquals(UiText.Resource(Res.string.error_unknown), DataError.Network.NOT_FOUND.toUiText())
        assertEquals(UiText.Resource(Res.string.error_unknown), DataError.Network.UNKNOWN.toUiText())
    }
}
