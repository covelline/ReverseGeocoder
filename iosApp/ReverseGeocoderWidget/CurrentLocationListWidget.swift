import WidgetKit
import SwiftUI

struct CurrentLocationListWidget: Widget {
    let kind: String = "CurrentLocationListWidget"
    
    var body: some WidgetConfiguration {
        StaticConfiguration(
            kind: kind,
            provider: Provider()
        ) { entry in
            CurrentLocationListWidgetEntryView(entry: entry)
                .containerBackground(.fill.tertiary, for: .widget)
        }
    }
}

struct CurrentLocationEntry: TimelineEntry {
    let date: Date
    let locationData: LocationData
}


fileprivate struct Provider: TimelineProvider {
    func placeholder(in context: Context) -> CurrentLocationEntry {
        return .init(
            date: .now,
            locationData: .placeholder)
    }
    
    
    func getTimeline(in context: Context, completion: @escaping (Timeline<CurrentLocationEntry>) -> Void) {
        Task {
            let locationData = try await LocationDataStore.shared.load()
            let entry = CurrentLocationEntry(
                date: locationData.timestamp ?? .now,
                locationData: locationData
            )
            
            let nextUpdate = Calendar
                .current
                .date(byAdding: .minute, value: 15, to: entry.date)!
            
            let timeLine = Timeline(entries: [entry], policy: .after(nextUpdate))
            completion(timeLine)
        }
    }
    
    func getSnapshot(in context: Context, completion: @escaping (Entry) -> Void) {
        let entry = CurrentLocationEntry(date: .now, locationData: .placeholder)
        completion(entry)
    }
}

struct CurrentLocationListWidgetEntryView: View {
    fileprivate var entry: Provider.Entry
    
    var body: some View {
        let locationData = entry.locationData
        VStack {
            VStack(alignment: .leading) {
                Spacer()
                switch locationData.jarlCityWardCountyCode {
                case .fetched(let code, let codeType):
                    let codeTypeName = switch codeType {
                    case .jcc, .ku:
                        "JCC"
                    case .jcg:
                        "JCG"
                    }
                    Text("\(codeTypeName): \(code)")
                default:
                    Spacer(minLength: 0)
                }
                switch locationData.administrativeArea {
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
                    Text(myCity)
                default:
                    Spacer(minLength: 0)
                }
                Spacer()
            }
            let dateFormatter = {
                let dateFormatter = DateFormatter()
                dateFormatter.locale = Locale.current
                dateFormatter.dateStyle = .short
                dateFormatter.timeStyle = .short
                dateFormatter.setLocalizedDateFormatFromTemplate("Mdjm")
                return dateFormatter
            }()
            let date = dateFormatter.string(from: entry.date)
            Spacer()
            HStack {
                Spacer()
                Text(date)
                    .font(.caption)
                Spacer()
            }
        }
    }
}

#Preview(as: .systemSmall) {
    CurrentLocationListWidget()
} timeline: {
    CurrentLocationEntry(date: .now, locationData: .placeholder)
}
