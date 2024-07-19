import Foundation

@MainActor
class LocationDataStore: ObservableObject {
    @Published var locationData: LocationData = .defaultValue
    
    static let shared: LocationDataStore = .init()
    
    private init() {}
    
    private static func fileURL() throws -> URL {
        let groupId = "group.com.covelline.Reversegeocoder"
        return FileManager
            .default
            .containerURL(forSecurityApplicationGroupIdentifier: groupId)!
            .appending(path: "location_data.data")
    }
    
    func load() async throws -> LocationData {
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
        return locationData
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

