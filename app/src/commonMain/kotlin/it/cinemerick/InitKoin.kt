package it.cinemerick

import it.cinemerick.core.data.coreDataModule
import it.cinemerick.core.data.platformCoreDataModule
import it.cinemerick.feature.showtimes.data.showtimesDataModule
import it.cinemerick.feature.showtimes.presentation.showtimesPresentationModule
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
