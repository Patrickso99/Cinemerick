package com.preichert.cinemerick

import com.preichert.cinemerick.core.assistant.AppleIntelligenceBridge

/**
 * Registers Apple Intelligence, called once by the Swift side of the app at start. FoundationModels is a Swift-only
 * framework that Kotlin cannot call, so Swift gives two functions: [availability] answers one of "available",
 * "appleIntelligenceNotEnabled", "modelNotReady", "deviceNotEligible" or "unavailable"; [respond] answers a request to
 * `onResult(text, errorCode)`, with the error code one of "unavailable", "guardrail", "context", "language",
 * "rateLimited" or "unknown", and a null text on an error.
 */
fun registerAppleIntelligence(
    availability: () -> String,
    respond: (String, String, (String?, String?) -> Unit) -> Unit,
) {
    AppleIntelligenceBridge.availability = availability
    AppleIntelligenceBridge.respond = respond
}
