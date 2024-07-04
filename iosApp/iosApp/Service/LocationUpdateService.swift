import Foundation
import CoreLocation
import GsiFeatureDB
import MapKit

@MainActor
class LocationUpdateService: NSObject {
    private let locationDataStore: LocationDataStore = .shared
    private var databaseProvider: DatabaseProvider = .shared
    private let locationManager = CLLocationManager()
    
    static let shared: LocationUpdateService = .init()
    
    override private init() {
        super.init()
        locationManager.delegate = self
        // 山の山頂など、微妙な位置での利用を想定しているのでBestを使う
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
    }
    
    /// ユーザーの操作によって現在位置情報を更新する処理
    func updateLocation(forceUpdate: Bool = false) async throws {
        let currentAuthorizationStatus = locationManager.authorizationStatus
        switch currentAuthorizationStatus {
        case .denied, .restricted:
            let locationData = LocationData(location: .error(reason: "Location.notAuthorized"), administrativeArea: .none, jarlCityWardCountyCode: .none, timestamp: Date())
            try await locationDataStore.save(locationData: locationData)
            return
        case .notDetermined:
            locationManager.requestWhenInUseAuthorization()
            return
        default:
            break
        }
        if !forceUpdate,
           let lastLocation = locationManager.location,
           lastLocation.timestamp.timeIntervalSinceNow < -1800 {
            try await updateLocationData(for: lastLocation)
            return
        }
        locationManager.requestLocation()
    }
    
    /// 得られた位置情報を使ってリバースジオコーディングなどを行う
    private func updateLocationData(for location: CLLocation) async throws {
        let database = try await databaseProvider.database
        var locationData = LocationData.defaultValue
        locationData.location = .fetched(latitude: location.coordinate.latitude, longitude: location.coordinate.longitude, altitude: location.altitude)
        locationData.timestamp = location.timestamp
        var jarlCodeID: KotlinInt? = nil
        
        do {
            if let administrativeArea = try await findAdministrativeArea(from: location.coordinate, in: database) {
                locationData.administrativeArea = .fetched(prefecture: administrativeArea.prefecture, subPrefecture: administrativeArea.subPrefecture, county: administrativeArea.county, city: administrativeArea.city, ward: administrativeArea.ward, code: administrativeArea.code)
                jarlCodeID = administrativeArea.jarlCityWardCountryCodeId
            } else {
                locationData.administrativeArea = .error(reason: "見つかりませんでした")
            }
        } catch {
            print("findAdministrativeArea error: \(error.localizedDescription)")
            locationData.administrativeArea = .error(reason: error.localizedDescription)
        }
        do {
            if let jarlCodeID = jarlCodeID,
               let jarlCode = try await {
                let jarlCityWardCountyCode = try await database.administrativeAreaDao().getJarlCityWardCountyCode(id: jarlCodeID.int32Value)
                return FoundJarlCityWardCountyCode.CodeType.findCode(jcc: jarlCityWardCountyCode.jccNumber, jcg: jarlCityWardCountyCode.jcgNumber, ku: jarlCityWardCountyCode.kuNumber)
            }() {
                locationData.jarlCityWardCountyCode = .fetched(code: jarlCode.code, codeType: jarlCode.codeType)
            } else {
                locationData.jarlCityWardCountyCode = .error(reason: "JCCナンバーが割り当てられていません")
            }
        } catch {
            print("getJarlCityWardCountyCode error: \(error.localizedDescription)")
            locationData.jarlCityWardCountyCode = .error(reason: error.localizedDescription)
        }
        
        try? await locationDataStore.save(locationData: locationData)
    }
    
    /// 緯度経度から行政区分を探す
    private func findAdministrativeArea(from coordinate: CLLocationCoordinate2D, in database: GsiFeatureDatabase) async throws -> AdministrativeArea? {
        let candidateArea = try await database
            .administrativeAreaDao()
            .getAdministrativeAreasInBounds(latitude: coordinate.latitude, longitude: coordinate.longitude)
        if candidateArea.isEmpty {
            return nil
        }
        let geoJsonDecoder = MKGeoJSONDecoder()
        let mapPoint = MKMapPoint(coordinate)
        let administrativeArea = candidateArea
            .first { area in
                guard let polygonData = area.polygon.data(using: .utf8),
                      let geometories = try? geoJsonDecoder.decode(polygonData),
                      let polygon = geometories.first as? MKPolygon else {
                    return false
                }
                let renderer = MKPolygonRenderer(polygon: polygon)
                let point = renderer.point(for: mapPoint)
                return renderer.path.contains(point)
            }
        return administrativeArea
    }
    
}

extension LocationUpdateService: CLLocationManagerDelegate {
    
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        Task {
            try? await updateLocation()
        }
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        Task {
            try? await updateLocationData(for:locations.last!)
        }
    }
    
    func locationManager(_ manager: CLLocationManager, didFailWithError error: any Error) {
        print("locationManager didFailWithError")
        print(error.localizedDescription)
    }
}
