package it.cinemerick.core.presentation

import it.cinemerick.core.domain.DataError
import it.cinemerick.core.presentation.resources.Res
import it.cinemerick.core.presentation.resources.error_access_denied
import it.cinemerick.core.presentation.resources.error_no_internet
import it.cinemerick.core.presentation.resources.error_serialization
import it.cinemerick.core.presentation.resources.error_server
import it.cinemerick.core.presentation.resources.error_timeout
import it.cinemerick.core.presentation.resources.error_unknown

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
