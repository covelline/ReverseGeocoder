import Foundation

/// アプリ全体で使う位置などに関する情報
struct LocationData: Codable {
    var location: Location
    var administrativeArea: FoundAdministrativeArea
    var jarlCityWardCountyCode: FoundJarlCityWardCountyCode
    var timestamp: Date?
}

extension LocationData {
    static let defaultValue: LocationData = .init(location: .none, administrativeArea: .none, jarlCityWardCountyCode: .none, timestamp: nil)
    
    static let placeholder: LocationData = .init(location: .fetched(latitude: 35.680882
                                                                           , longitude: 139.767408, altitude: 0.0), administrativeArea: .fetched(prefecture: "東京都", subPrefecture: nil, county: nil, city: nil, ward: "千代田区", code: ""), jarlCityWardCountyCode: .fetched(code: "100101", codeType: .jcc))
}

enum Location: Codable {
    case error(reason: String)
    case fetched(latitude: Double, longitude: Double, altitude: Double)
    case none
}

enum FoundAdministrativeArea: Codable {
    case error(reason: String)
    case fetched(prefecture: String, subPrefecture: String?, county: String?, city: String?, ward: String?, code: String)
    case none
}

enum FoundJarlCityWardCountyCode: Codable {
    case error(reason: String)
    case fetched(code: String, codeType: CodeType)
    case none
    enum CodeType: Codable {
        case jcc
        case jcg
        case ku
    }
}

extension FoundJarlCityWardCountyCode.CodeType {
    
    static func findCode(jcc: String?, jcg: String?, ku: String?) -> (code: String, codeType: FoundJarlCityWardCountyCode.CodeType)? {
        if let jcc = jcc {
            return (jcc, .jcc)
        } else if let jcg = jcg {
            return (jcg, .jcg)
        } else if let ku = ku {
            return (ku, .ku)
        }
        return nil
    }
}
