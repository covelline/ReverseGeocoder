package com.covelline.reversegeocoder.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.MultiProcessDataStoreFactory
import com.covelline.reversegeocoder.LocationData
import com.covelline.reversegeocoder.data.LocationDataSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

  private lateinit var locationDataStore: DataStore<LocationData>

  @Provides
  @Singleton
  fun providesLocationDataStore(@ApplicationContext context: Context): DataStore<LocationData> =
    if (this::locationDataStore.isInitialized) locationDataStore
    else {
      locationDataStore =
        MultiProcessDataStoreFactory.create(
          serializer = LocationDataSerializer,
          produceFile = { File(context.filesDir, "location_data.pb") },
        )
      locationDataStore
    }
}
