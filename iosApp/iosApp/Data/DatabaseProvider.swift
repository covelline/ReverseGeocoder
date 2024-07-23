import Foundation
import GsiFeatureDB

/// Room database のインスタンスを提供する
struct DatabaseProvider {
    static let shared: DatabaseProvider = .init()
    private init() {}

    private var cachedDatabase: GsiFeatureDatabase?

    var database: GsiFeatureDatabase {
        mutating get async throws {
            if let cached = cachedDatabase {
                return cached
            }
            let dbBundleRequest = NSBundleResourceRequest(tags: ["gsi_feature"])
            if await !dbBundleRequest.conditionallyBeginAccessingResources() {
                try await dbBundleRequest.beginAccessingResources()
            }
            guard let originalDatabase = Bundle.main.path(forResource: GsiFeatureDatabase.companion.DB_FILE_NAME, ofType: nil) else {
                fatalError("オリジナルデータベースが見つかりません")
            }
            guard let documentPath = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first else {
                fatalError("ドキュメントディレクトリが見つかりません")
            }
            let destinationPath = documentPath.appending(path: GsiFeatureDatabase.companion.DB_FILE_NAME)
            if !FileManager.default.fileExists(atPath: destinationPath.path()) {
                try FileManager.default.copyItem(atPath: originalDatabase, toPath: destinationPath.path())
            }
            let database = GsiFeatureDatabaseBuilder(
                databasePath: destinationPath.path(),
                originalDbPath: originalDatabase
            ).createGsiFeatureDatabase()
            cachedDatabase = database
            return database
        }
    }
}
