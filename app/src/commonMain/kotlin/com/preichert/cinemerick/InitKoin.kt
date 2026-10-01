package com.preichert.cinemerick

import com.preichert.cinemerick.core.data.coreDataModule
import com.preichert.cinemerick.core.data.platformCoreDataModule
import com.preichert.cinemerick.feature.showtimes.data.showtimesDataModule
import com.preichert.cinemerick.feature.showtimes.presentation.showtimesPresentationModule
import org.koin.mp.KoinPlatform
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    if (KoinPlatform.getKoinOrNull() != null) return
    startKoin {
        config?.invoke(this)
        modules(
            coreDataModule,
            platformCoreDataModule,
            showtimesDataModule,
            showtimesPresentationModule
        )
    }
}
