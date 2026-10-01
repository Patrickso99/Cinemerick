package com.preichert.cinemerick.core.presentation

import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.presentation.resources.Res
import com.preichert.cinemerick.core.presentation.resources.error_access_denied
import com.preichert.cinemerick.core.presentation.resources.error_no_internet
import com.preichert.cinemerick.core.presentation.resources.error_serialization
import com.preichert.cinemerick.core.presentation.resources.error_server
import com.preichert.cinemerick.core.presentation.resources.error_timeout
import com.preichert.cinemerick.core.presentation.resources.error_unknown

fun DataError.toUiText(): UiText {
    val resource = when (this) {
        DataError.Network.NO_INTERNET -> Res.string.error_no_internet
        DataError.Network.REQUEST_TIMEOUT -> Res.string.error_timeout
        DataError.Network.SERVER_ERROR,
        DataError.Network.SERVICE_UNAVAILABLE -> Res.string.error_server
        DataError.Network.UNAUTHORIZED,
        DataError.Network.FORBIDDEN -> Res.string.error_access_denied
        DataError.Network.SERIALIZATION -> Res.string.error_serialization
        else -> Res.string.error_unknown
    }
    return UiText.Resource(resource)
}
