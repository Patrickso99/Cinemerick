package com.preichert.cinemerick.feature.settings.presentation.key

import com.preichert.cinemerick.core.domain.assistant.CloudProvider

sealed interface AssistantKeyAction {
    data class OnProviderSelect(val provider: CloudProvider) : AssistantKeyAction
    data class OnKeyChange(val key: String) : AssistantKeyAction
    data class OnModelChange(val model: String) : AssistantKeyAction
    data object OnKeyVisibilityToggle : AssistantKeyAction
    data object OnSaveClick : AssistantKeyAction
    data object OnUseSavedKeyClick : AssistantKeyAction
}
