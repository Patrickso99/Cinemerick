import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        AppleIntelligence.register()
        InitKoinKt.doInitKoin(config: nil)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .ignoresSafeArea()
        }
    }
}
