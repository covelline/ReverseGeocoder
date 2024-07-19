import SwiftUI

struct MainNavigation: View {
    @Binding var locationData: LocationData
    
    var body: some View {
        NavigationStack {
            CurrentLocationListScreen(locationData: $locationData)
        }
    }
    
}
