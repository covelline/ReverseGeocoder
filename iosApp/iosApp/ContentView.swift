import SwiftUI
import GsiFeatureDB

struct ContentView: View {
    @State private var showContent = true
    @Binding var locationData: LocationData
    
    var body: some View {
        VStack {
            Button("Click me!") {
                withAnimation {
                    showContent = !showContent
                }
            }

            if showContent {
                VStack(spacing: 16) {
                    Image(systemName: "swift")
                        .font(.system(size: 200))
                        .foregroundColor(.accentColor)
                    Text("\(locationData.location)\n\(locationData.administrativeArea)\n\(locationData.jarlCityWardCountyCode)")
                }
                .transition(.move(edge: .top).combined(with: .opacity))
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .padding()
        .task {
            Task {
                try await LocationUpdateService.shared.updateLocation()
            }
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView(
            locationData: .constant(.defaultValue)
        )
    }
}
