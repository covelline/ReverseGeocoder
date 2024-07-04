import Foundation

@MainActor
class LocationDataStore: ObservableObject {
    @Published var locationData: LocationData = .defaultValue
    
    static let shared: LocationDataStore = .init()
    
    private init() {}
    
    private static func fileURL() throws -> URL {
        try FileManager.default.url(for: .documentDirectory,
                                    in: .userDomainMask,
                                    appropriateFor: nil,
                                    create: false)
        .appending(path: "location_data.data")
    }
    
    func load() async throws {
        let task = Task<LocationData, Error> {
            let fileURL = try Self.fileURL()
            guard let data = try? Data(contentsOf: fileURL) else {
                return .defaultValue
            }
            let locationData = try JSONDecoder().decode(LocationData.self, from: data)
            return locationData
        }
        let locationData = try await task.value
        self.locationData = locationData
    }
    
    func save(locationData: LocationData) async throws {
        let task = Task {
            let data = try JSONEncoder().encode(locationData)
            let outFile = try Self.fileURL()
            try data.write(to: outFile)
        }
        _ = try await task.value
        self.locationData = locationData
    }
}

