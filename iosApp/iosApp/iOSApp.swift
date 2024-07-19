import SwiftUI

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    @StateObject private var locationDataStore: LocationDataStore = .shared
    
    var body: some Scene {
        WindowGroup {
            MainNavigation(locationData: $locationDataStore.locationData)
                .task {
                    do {
                        let _ = try await locationDataStore.load()
                    } catch {
                        fatalError(error.localizedDescription)
                    }
                }
        }
    }
}
