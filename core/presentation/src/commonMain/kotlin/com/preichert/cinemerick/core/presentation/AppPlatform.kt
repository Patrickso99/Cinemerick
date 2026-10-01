package com.preichert.cinemerick.core.presentation

enum class AppPlatform { ANDROID, IOS, DESKTOP, WEB }

expect val currentPlatform: AppPlatform
