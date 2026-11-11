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
import assertk.assertThat
import assertk.assertions.isEqualTo

class DataErrorToUiTextTest {

    @Test
    fun connectionProblemsSayWhatHappened() {
        assertThat(DataError.Network.NO_INTERNET.toUiText()).isEqualTo(UiText.Resource(Res.string.error_no_internet))
        assertThat(DataError.Network.REQUEST_TIMEOUT.toUiText()).isEqualTo(UiText.Resource(Res.string.error_timeout))
    }

    @Test
    fun serverProblemsShareOneMessage() {
        assertThat(DataError.Network.SERVER_ERROR.toUiText()).isEqualTo(UiText.Resource(Res.string.error_server))
        assertThat(DataError.Network.SERVICE_UNAVAILABLE.toUiText()).isEqualTo(UiText.Resource(Res.string.error_server))
    }

    @Test
    fun refusedAccessSaysSo() {
        assertThat(DataError.Network.UNAUTHORIZED.toUiText()).isEqualTo(UiText.Resource(Res.string.error_access_denied))
        assertThat(DataError.Network.FORBIDDEN.toUiText()).isEqualTo(UiText.Resource(Res.string.error_access_denied))
    }

    @Test
    fun aBrokenResponseIsReportedAsSerialization() {
        assertThat(DataError.Network.SERIALIZATION.toUiText()).isEqualTo(UiText.Resource(Res.string.error_serialization))
    }

    @Test
    fun everythingElseIsUnknown() {
        assertThat(DataError.Network.NOT_FOUND.toUiText()).isEqualTo(UiText.Resource(Res.string.error_unknown))
        assertThat(DataError.Network.UNKNOWN.toUiText()).isEqualTo(UiText.Resource(Res.string.error_unknown))
    }
}
