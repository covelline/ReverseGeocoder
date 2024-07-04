package com.covelline.reversegeocoder.receiver

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.covelline.reversegeocoder.ui.widget.CurrentLocationListWidget
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CurrentLocationWidgetReceiver : GlanceAppWidgetReceiver() {
  override val glanceAppWidget: GlanceAppWidget
    get() {
      return CurrentLocationListWidget()
    }
}
