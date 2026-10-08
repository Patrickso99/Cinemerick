package com.preichert.cinemerick.core.domain

interface AnimationPreferences {
    fun animationsEnabled(): Boolean
    fun setAnimationsEnabled(enabled: Boolean)
}
