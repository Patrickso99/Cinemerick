import Foundation
import ComposeApp
#if canImport(FoundationModels)
import FoundationModels
#endif

/// Apple Intelligence for the Kotlin side of the app. FoundationModels is Swift-only, so the app registers two
/// functions at start (`registerAppleIntelligence`): one tells whether the model can be used, one answers a request.
/// Before iOS 26, or on a device without Apple Intelligence, both say that there is none.
enum AppleIntelligence {
    static func register() {
        AppleIntelligenceKt.registerAppleIntelligence(
            availability: { availability() },
            respond: { system, user, done in respond(system: system, user: user, done: done) }
        )
    }

    private static func availability() -> String {
        #if canImport(FoundationModels)
        if #available(iOS 26.0, *) {
            switch SystemLanguageModel.default.availability {
            case .available:
                return "available"
            case .unavailable(let reason):
                switch reason {
                case .appleIntelligenceNotEnabled: return "appleIntelligenceNotEnabled"
                case .modelNotReady: return "modelNotReady"
                case .deviceNotEligible: return "deviceNotEligible"
                @unknown default: return "unavailable"
                }
            }
        }
        #endif
        return "unavailable"
    }

    /// Answers `system` and `user` to `done(text, errorCode)`, exactly once.
    private static func respond(system: String, user: String, done: @escaping (String?, String?) -> KotlinUnit) {
        #if canImport(FoundationModels)
        if #available(iOS 26.0, *) {
            Task {
                do {
                    // A new session for each request: a request has nothing to do with the previous one.
                    let session = LanguageModelSession(model: .default, instructions: system)
                    let response = try await session.respond(to: user)
                    _ = done(response.content, nil)
                } catch {
                    _ = done(nil, errorCode(for: error))
                }
            }
            return
        }
        #endif
        _ = done(nil, "unavailable")
    }

    /// The errors differ between iOS 26 (`GenerationError`) and 27 (`LanguageModelError`) but their cases are named
    /// alike: they are told apart by name, which holds for both.
    private static func errorCode(for error: Error) -> String {
        let name = String(describing: error).lowercased()
        if name.contains("guardrail") || name.contains("refusal") { return "guardrail" }
        if name.contains("contextwindow") || name.contains("contextsize") { return "context" }
        if name.contains("unsupportedlanguage") { return "language" }
        if name.contains("ratelimited") { return "rateLimited" }
        if name.contains("assetsunavailable") { return "unavailable" }
        return "unknown"
    }
}
