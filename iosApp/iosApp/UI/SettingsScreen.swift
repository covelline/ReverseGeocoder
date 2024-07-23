import SwiftUI

struct SettingsScreen: View {
    var body: some View {
        List {
            Section {
                HStack {
                    Text("SettingsScreen.version.appVersion")
                    Spacer()
                    Text(Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? "")
                }
                VStack(alignment: .leading) {
                    Text("SettingsScreen.version.gsiVersion")
                    Text("https://nlftp.mlit.go.jp/ksj/gml/datalist/KsjTmplt-N03-2024.html")
                }
            }
        }
        .navigationTitle("SettingsScreen.title")
    }
}

#Preview {
    SettingsScreen()
}
