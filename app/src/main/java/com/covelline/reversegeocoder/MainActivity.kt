package com.covelline.reversegeocoder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.work.WorkManager
import com.covelline.reversegeocoder.navigation.MainNavigation
import com.covelline.reversegeocoder.ui.theme.ReverseGeocoderTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  @Inject lateinit var workManager: WorkManager

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent { ReverseGeocoderTheme { MainNavigation() } }
  }
}
