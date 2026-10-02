package com.preichert.cinemerick.core.presentation

enum class AppPlatform { ANDROID, IOS, DESKTOP }

expect val currentPlatform: AppPlatform
