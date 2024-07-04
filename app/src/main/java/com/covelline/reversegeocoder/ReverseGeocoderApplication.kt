package com.covelline.reversegeocoder

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import timber.log.Timber

@HiltAndroidApp
class ReverseGeocoderApplication : Application(), Configuration.Provider {

  @Inject lateinit var hiltWorkerFactory: HiltWorkerFactory

  override fun onCreate() {
    super.onCreate()
    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    }
  }

  override val workManagerConfiguration: Configuration
    get() = Configuration.Builder().setWorkerFactory(hiltWorkerFactory).build()
}
