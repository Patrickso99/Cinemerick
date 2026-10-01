package it.cinemerick.feature.showtimes.presentation

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val showtimesPresentationModule = module {
    viewModelOf(::ShowtimesViewModel)
}
