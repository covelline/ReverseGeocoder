import SwiftUI

/// 現在地情報画面
struct CurrentLocationListScreen: View {
    @Binding var locationData: LocationData
    var body: some View {
        List {
            Section {
                HStack {
                    Image(systemName: "doc.plaintext")
                    VStack(
                        alignment: .leading
                    ) {
                        switch locationData.administrativeArea {
                        case .error(let reason):
                            Text("CurrentLocationListScreen.administrativeArea.error\(reason)")
                        case .none:
                            Text("CurrentLocationListScreen.administrativeArea.none")
                        case .fetched(let prefecture, let subPrefecture, let county, let city, let ward, _):
                            let myCity = [
                                prefecture,
                                subPrefecture,
                                county,
                                city,
                                ward
                            ]
                                .compactMap { $0 }
                                .joined()
                            Text("CurrentLocationListScreen.administrativeArea.myCity: \(myCity)")
                        }
                        switch locationData.jarlCityWardCountyCode {
                        case .error(let reason):
                            Text("CurrentLocationListScreen.jarlCityWardCountyCode.error\(reason)")
                        case .none:
                            Text("CurrentLocationListScreen.jarlCityWardCountyCode.none")
                        case .fetched(let code, let codeType):
                            let codeTypeName = switch codeType {
                                case .jcc, .ku:
                                    "JCC"
                                case .jcg:
                                    "JCG"
                            }
                            Text("\(codeTypeName): \(code)")
                        }
                    }
                }
                HStack {
                    Image(systemName: "mappin.circle")
                    VStack(
                        alignment: .leading
                    ) {
                        switch locationData.location {
                        case .error(let reason):
                            Text("CurrentLocationListScreen.location.error\(reason)")
                        case .none:
                            Text("CurrentLocationListScreen.location.none")
                        case .fetched(let latitude, let longitude, let altitude):
                            Text("CurrentLocationListScreen.location.latitude: \(String(format: "%.3f", latitude))")
                            Text("CurrentLocationListScreen.location.longitude: \(String(format: "%.3f", longitude))")
                            Text("CurrentLocationListScreen.location.altitude: \(String(format: "%.2f", altitude))")
                        }
                    }
                }
            }
        }
        .listStyle(.insetGrouped)
        .navigationTitle("CurrentLocationListScreen.title")
        .toolbar(content: {
            ToolbarItem(placement: .topBarTrailing) {
                NavigationLink {
                    SettingsScreen()
                } label: {
                    Image(systemName: "gear")
                        .imageScale(.large)
                }
            }
            ToolbarItem(placement: .status) {
                if let timestamp = locationData.timestamp {
                    let dateFormatter = {
                        let dateFormatter = DateFormatter()
                        dateFormatter.locale = Locale.current
                        dateFormatter.dateStyle = .long
                        dateFormatter.timeStyle = .medium
                        return dateFormatter
                    }()
                    let formatted = dateFormatter.string(from: timestamp)
                    Text("CurrentLocationListScreen.location.timestamp\(formatted)")
                }
            }
        })
        .task {
            Task {
                try await LocationUpdateService.shared.updateLocation()
            }
        }
    }

}

#Preview {
    CurrentLocationListScreen(
        locationData: .constant(.defaultValue)
    )
}

#Preview {
    CurrentLocationListScreen(locationData: .constant(.init(location: .fetched(latitude: 35.681236, longitude: 139.767125, altitude: 0.0), administrativeArea: .fetched(prefecture: "東京都", subPrefecture: nil, county: nil, city: "千代田区", ward: nil, code: ""), jarlCityWardCountyCode: .fetched(code: "11011", codeType: .jcc), timestamp: Date())))
}


