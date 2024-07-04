package com.covelline.reversegeocoder.di

import android.content.Context
import com.covelline.reversegeocoder.data.FusedLocationRepository
import com.covelline.reversegeocoder.data.LocationRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationModule {

  @Binds
  @Singleton
  abstract fun bindLocationRepository(
    locationRepository: FusedLocationRepository
  ): LocationRepository
}

@Module
@InstallIn(SingletonComponent::class)
object LocationServiceModule {

  @Provides
  @Singleton
  fun providesFusedLocationProviderClient(
    @ApplicationContext context: Context
  ): FusedLocationProviderClient {
    return LocationServices.getFusedLocationProviderClient(context)
  }
}
