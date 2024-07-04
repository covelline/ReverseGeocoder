import SwiftUI

enum Destination: Hashable {
    case settings
}

struct MainNavigation: View {
    @Binding var locationData: LocationData
    
    var body: some View {
        NavigationStack {
            CurrentLocationListScreen(locationData: $locationData)
        }
            .navigationDestination(for: Destination.self) { destination in
                switch destination {
                case .settings:
                    Text("settings")
                }
            }
    }
    
}
