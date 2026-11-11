package com.preichert.cinemerick

import com.preichert.cinemerick.core.data.coreDataModule
import com.preichert.cinemerick.core.presentation.corePresentationModule
import com.preichert.cinemerick.feature.showtimes.data.showtimesDataModule
import com.preichert.cinemerick.feature.showtimes.presentation.showtimesPresentationModule
import com.preichert.cinemerick.feature.settings.presentation.settingsPresentationModule
import co.touchlab.kermit.Logger
import com.preichert.cinemerick.core.domain.BuildKonfig
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    Logger.setTag(BuildKonfig.APP_NAME)
    Logger.i { "${BuildKonfig.APP_NAME} v${BuildKonfig.VERSION_NAME} (${BuildKonfig.VERSION_CODE})" }
    startKoin {
        config?.invoke(this)
        modules(
            coreDataModule,
            corePresentationModule,
            showtimesDataModule,
            showtimesPresentationModule,
            settingsPresentationModule
        )
    }
}
