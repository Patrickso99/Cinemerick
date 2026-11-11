package com.preichert.cinemerick.core.testing

import kotlin.time.Clock
import kotlin.time.Instant

/** A clock that only moves when a test sets [millis], the time since the epoch. */
class FakeClock(var millis: Long = 0L) : Clock {
    override fun now(): Instant = Instant.fromEpochMilliseconds(millis)
}
