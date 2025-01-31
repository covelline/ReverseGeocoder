import CoreLocation
import Foundation
import GsiFeatureDB
import MapKit
import WidgetKit

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

    /// 現在位置情報の監視を開始する
    func updateLocation() async throws {
        guard try await checkLocationAuthorizationStatus() else {
            return
        }
        if let lastLocation = locationManager.location,
           lastLocation.timestamp.timeIntervalSinceNow < -1800
        {
            try await updateLocationData(for: lastLocation)
        }
        for try await locationUpdate in CLLocationUpdate.liveUpdates() {
            if let location = locationUpdate.location {
                try await updateLocationData(for: location)
                WidgetCenter.shared.reloadAllTimelines()
            }
        }
    }

    /// 得られた位置情報を使ってリバースジオコーディングなどを行う
    private func updateLocationData(for location: CLLocation) async throws {
        let database = try await databaseProvider.database
        var locationData = LocationData.defaultValue
        locationData.location = .fetched(latitude: location.coordinate.latitude, longitude: location.coordinate.longitude, altitude: location.altitude)
        locationData.timestamp = location.timestamp
        var jarlCodeID: KotlinInt?

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
            if let jarlCodeID,
               let jarlCode = try await {
                   let jarlCityWardCountyCode = try await database.administrativeAreaDao().getJarlCityWardCountyCode(id: jarlCodeID.int32Value)
                   return FoundJarlCityWardCountyCode.CodeType.findCode(jcc: jarlCityWardCountyCode.jccNumber, jcg: jarlCityWardCountyCode.jcgNumber, ku: jarlCityWardCountyCode.kuNumber)
               }()
            {
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
                      let polygon = geometories.first as? MKPolygon
                else {
                    return false
                }
                let renderer = MKPolygonRenderer(polygon: polygon)
                let point = renderer.point(for: mapPoint)
                return renderer.path.contains(point)
            }
        return administrativeArea
    }

    /// 位置情報取得許可状況をチェックする
    /// true なら許可済
    private func checkLocationAuthorizationStatus() async throws -> Bool {
        let currentAuthorizationStatus = locationManager.authorizationStatus
        switch currentAuthorizationStatus {
        case .denied, .restricted:
            let locationData = LocationData(location: .error(reason: "Location.notAuthorized"), administrativeArea: .none, jarlCityWardCountyCode: .none, timestamp: Date())
            try await locationDataStore.save(locationData: locationData)
            return false
        case .notDetermined:
            locationManager.requestWhenInUseAuthorization()
            return false
        case .authorizedAlways, .authorizedWhenInUse:
            return true
        @unknown default:
            // コンパイルエラー対策
            return true
        }
    }
}

extension LocationUpdateService: CLLocationManagerDelegate {
    func locationManagerDidChangeAuthorization(_: CLLocationManager) {
        Task {
            try? await updateLocation()
        }
    }

    func locationManager(_: CLLocationManager, didFailWithError error: any Error) {
        print("locationManager didFailWithError")
        print(error.localizedDescription)
    }
}
