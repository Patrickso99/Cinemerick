package com.preichert.cinemerick.core.presentation

import androidx.compose.runtime.Stable
import com.preichert.cinemerick.core.domain.AnimationPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Stable
class AnimationController(
    private val animationPreferences: AnimationPreferences,
) {
    private val _isEnabled = MutableStateFlow(animationPreferences.animationsEnabled())
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    fun setEnabled(isEnabled: Boolean) {
        _isEnabled.value = isEnabled
        animationPreferences.setAnimationsEnabled(isEnabled)
    }
}
