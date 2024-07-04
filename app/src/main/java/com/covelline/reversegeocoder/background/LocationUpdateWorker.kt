package com.covelline.reversegeocoder.background

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.covelline.reversegeocoder.FindAdministrativeAreaErrorReason
import com.covelline.reversegeocoder.JarlCityWardCountyCodeType
import com.covelline.reversegeocoder.LocationData
import com.covelline.reversegeocoder.UpdateLocationErrorReason
import com.covelline.reversegeocoder.administrativeArea
import com.covelline.reversegeocoder.copy
import com.covelline.reversegeocoder.data.AdministrativeArea
import com.covelline.reversegeocoder.data.GsiFeatureDatabase
import com.covelline.reversegeocoder.data.LocationRepository
import com.covelline.reversegeocoder.findAdministrativeAreaError
import com.covelline.reversegeocoder.jarlCityWardCountyCode
import com.covelline.reversegeocoder.location
import com.covelline.reversegeocoder.locationData
import com.covelline.reversegeocoder.ui.widget.CurrentLocationListWidget
import com.covelline.reversegeocoder.updateLocationError
import com.google.protobuf.timestamp
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlin.Result as kotlinResult
import kotlin.time.Duration.Companion.milliseconds
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.io.geojson.GeoJsonReader
import timber.log.Timber

@HiltWorker
class LocationUpdateWorker
@AssistedInject
constructor(
  @Assisted appContext: Context,
  @Assisted workerParams: WorkerParameters,
  private val locationRepository: LocationRepository,
  private val dataStore: DataStore<LocationData>,
  private val gsiFeatureDatabase: GsiFeatureDatabase,
) : CoroutineWorker(appContext, workerParams) {

  companion object {
    /** 強制的に現在位置をセンサーから取得したい場合、このキーをtrueにしてInputDataを設定する */
    const val FORCE_UPDATE_KEY = "force_update"
  }

  @RequiresPermission(
    anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION]
  )
  override suspend fun doWork(): Result {
    val foundLocation =
      updateLocation()
        .onFailure { error ->
          val reason =
            when (error) {
              is SecurityException -> UpdateLocationErrorReason.PERMISSION_DENIED
              else -> UpdateLocationErrorReason.API_ERROR
            }
          dataStore.updateData {
            locationData {
              updateLocationError = updateLocationError {
                this@updateLocationError.reason = reason
                message = error.message ?: ""
              }
            }
          }
          return Result.failure()
        }
        .getOrNull()
    if (foundLocation == null) {
      Timber.d("cannot get location")
      return Result.retry()
    }
    dataStore.updateData {
      locationData {
        location = location {
          latitude = foundLocation.latitude
          longitude = foundLocation.longitude
          altitude = foundLocation.altitude
        }
        timestamp = timestamp {
          val locationTime = foundLocation.time.milliseconds.inWholeSeconds
          seconds = locationTime
        }
      }
    }
    val foundAdministrativeArea = findAdministrativeArea(foundLocation)
    if (foundAdministrativeArea?.jarlCityWardCountryCodeId == null) {
      dataStore.updateData {
        it.copy {
          findAdministrativeAreaError = findAdministrativeAreaError {
            reason = FindAdministrativeAreaErrorReason.NOT_FOUND_ADMINISTRATIVE_AREA
          }
        }
      }
      CurrentLocationListWidget().updateAll(applicationContext)
      return Result.success()
    }
    val jarlCodeId =
      requireNotNull(foundAdministrativeArea.jarlCityWardCountryCodeId) {
        "すでにnullチェックをしたので、ここでは非nullであるはずです。"
      }

    val foundJarlCode =
      gsiFeatureDatabase.administrativeAreaDao().getJarlCityWardCountyCode(jarlCodeId)
    dataStore.updateData {
      it.copy {
        administrativeArea = administrativeArea {
          prefecture = foundAdministrativeArea.prefecture
          subPrefecture = foundAdministrativeArea.subPrefecture ?: ""
          county = foundAdministrativeArea.county ?: ""
          city = foundAdministrativeArea.city ?: ""
          ward = foundAdministrativeArea.ward ?: ""
          code = foundAdministrativeArea.code
        }
        jarlCityWardCountyCode = jarlCityWardCountyCode {
          code =
            requireNotNull(
              foundJarlCode.jccNumber ?: foundJarlCode.jcgNumber ?: foundJarlCode.kuNumber
            ) {
              "jcc, jcg, kuのいずれかがセットされているはず"
            }
          type =
            when {
              foundJarlCode.jccNumber != null -> JarlCityWardCountyCodeType.JCC
              foundJarlCode.jcgNumber != null -> JarlCityWardCountyCodeType.JCG
              else -> JarlCityWardCountyCodeType.Ku
            }
        }
      }
    }
    CurrentLocationListWidget().updateAll(applicationContext)
    return Result.success()
  }

  /** 位置情報取得処理。何らかの理由で位置情報がnullを返したら、Workerは再起動とする */
  private suspend fun updateLocation(): kotlinResult<Location?> {
    if (
      ContextCompat.checkSelfPermission(
        applicationContext,
        Manifest.permission.ACCESS_FINE_LOCATION,
      ) != PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
          applicationContext,
          Manifest.permission.ACCESS_COARSE_LOCATION,
        ) != PackageManager.PERMISSION_GRANTED
    ) {
      Timber.d("location permission is not granted")
      return kotlinResult.failure(SecurityException())
    }
    val forcedUpdate = inputData.getBoolean(FORCE_UPDATE_KEY, false)
    val locationResult =
      if (forcedUpdate) {
        runCatching { locationRepository.getCurrentLocation() }
      } else {
        runCatching { locationRepository.getLastKnownLocation() }
      }
    if (locationResult.isFailure) {
      Timber.e(locationResult.exceptionOrNull())
      return kotlinResult.failure(locationResult.exceptionOrNull()!!)
    }
    return kotlinResult.success(locationResult.getOrNull())
  }

  private suspend fun findAdministrativeArea(location: Location): AdministrativeArea? {
    val candidateArea =
      gsiFeatureDatabase
        .administrativeAreaDao()
        .getAdministrativeAreasInBounds(location.latitude, location.longitude)
    if (candidateArea.isEmpty()) {
      return null
    }
    val point = GeometryFactory().createPoint(Coordinate(location.longitude, location.latitude))
    val geoJsonReader = GeoJsonReader(GeometryFactory())
    val administrativeArea = candidateArea.find { geoJsonReader.read(it.polygon).contains(point) }
    return administrativeArea
  }
}
