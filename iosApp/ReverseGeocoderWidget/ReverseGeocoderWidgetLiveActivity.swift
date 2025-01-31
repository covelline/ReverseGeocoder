import ActivityKit
import SwiftUI
import WidgetKit

struct ReverseGeocoderWidgetAttributes: ActivityAttributes {
    public struct ContentState: Codable, Hashable {
        // Dynamic stateful properties about your activity go here!
        var emoji: String
    }

    // Fixed non-changing properties about your activity go here!
    var name: String
}

struct ReverseGeocoderWidgetLiveActivity: Widget {
    var body: some WidgetConfiguration {
        ActivityConfiguration(for: ReverseGeocoderWidgetAttributes.self) { context in
            // Lock screen/banner UI goes here
            VStack {
                Text("Hello \(context.state.emoji)")
            }
            .activityBackgroundTint(Color.cyan)
            .activitySystemActionForegroundColor(Color.black)

        } dynamicIsland: { context in
            DynamicIsland {
                // Expanded UI goes here.  Compose the expanded UI through
                // various regions, like leading/trailing/center/bottom
                DynamicIslandExpandedRegion(.leading) {
                    Text("Leading")
                }
                DynamicIslandExpandedRegion(.trailing) {
                    Text("Trailing")
                }
                DynamicIslandExpandedRegion(.bottom) {
                    Text("Bottom \(context.state.emoji)")
                    // more content
                }
            } compactLeading: {
                Text("L")
            } compactTrailing: {
                Text("T \(context.state.emoji)")
            } minimal: {
                Text(context.state.emoji)
            }
            .widgetURL(URL(string: "http://www.apple.com"))
            .keylineTint(Color.red)
        }
    }
}

private extension ReverseGeocoderWidgetAttributes {
    static var preview: ReverseGeocoderWidgetAttributes {
        ReverseGeocoderWidgetAttributes(name: "World")
    }
}

private extension ReverseGeocoderWidgetAttributes.ContentState {
    static var smiley: ReverseGeocoderWidgetAttributes.ContentState {
        ReverseGeocoderWidgetAttributes.ContentState(emoji: "😀")
    }

    static var starEyes: ReverseGeocoderWidgetAttributes.ContentState {
        ReverseGeocoderWidgetAttributes.ContentState(emoji: "🤩")
    }
}

#Preview("Notification", as: .content, using: ReverseGeocoderWidgetAttributes.preview) {
    ReverseGeocoderWidgetLiveActivity()
} contentStates: {
    ReverseGeocoderWidgetAttributes.ContentState.smiley
    ReverseGeocoderWidgetAttributes.ContentState.starEyes
}
