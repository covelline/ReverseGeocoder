//
//  AppIntent.swift
//  ReverseGeocoderWidget
//
//  Created by Takaya Funabiki on 2024/07/17.
//  Copyright © 2024 orgName. All rights reserved.
//

import WidgetKit
import AppIntents

struct ConfigurationAppIntent: WidgetConfigurationIntent {
    static var title: LocalizedStringResource = "Configuration"
    static var description = IntentDescription("This is an example widget.")

    // An example configurable parameter.
    @Parameter(title: "Favorite Emoji", default: "😃")
    var favoriteEmoji: String
}
