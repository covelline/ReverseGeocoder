package com.covelline.reversegeocoder.di

import android.content.Context
import com.covelline.reversegeocoder.data.GsiFeatureDatabase
import com.covelline.reversegeocoder.data.GsiFeatureDatabaseBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

  @Provides
  @Singleton
  fun provideGsiFeatureDatabase(@ApplicationContext context: Context): GsiFeatureDatabase {
    return GsiFeatureDatabaseBuilder(context).createGsiFeatureDatabase()
  }
}
