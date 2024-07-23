import FirebaseCore
import SwiftUI

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_: UIApplication, didFinishLaunchingWithOptions _: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {
        if let googlerServicesPlist = Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist"),
           let firebaseOptions = FirebaseOptions(contentsOfFile: googlerServicesPlist)
        {
            FirebaseApp.configure(options: firebaseOptions)
        }
        return true
    }
}
