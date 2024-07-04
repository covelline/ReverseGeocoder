package com.covelline.reversegeocoder.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.SystemClock
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.core.location.LocationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.tasks.await

interface LocationRepository {
  @RequiresPermission(
    anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION]
  )
  /** 現在の位置情報を取得する システムが保持する位置情報を使って、必要最小限のリソースで取得する */
  suspend fun getLastKnownLocation(): Location?

  @RequiresPermission(
    anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION]
  )
  /** センサーを呼び出して強制的に現在位置を取得する */
  suspend fun getCurrentLocation(): Location?
}

class FusedLocationRepository
@Inject
constructor(
  @ApplicationContext private val context: Context,
  private val fusedLocationClient: FusedLocationProviderClient,
) : LocationRepository {

  companion object {
    private val LOCATION_EXPIRE_DURATION = 10.minutes
  }

  @RequiresPermission(
    anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION]
  )
  override suspend fun getLastKnownLocation(): Location? {
    val lastLocation = fusedLocationClient.lastLocation.await()
    val isExpired = lastLocation?.isLocationExpired()
    // 取得した位置情報が最新ならそのまま使う
    if (isExpired == false) {
      return lastLocation
    }
    return getCurrentLocation()
  }

  @RequiresPermission(
    anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION]
  )
  override suspend fun getCurrentLocation(): Location? {
    val priority =
      when {
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
          PackageManager.PERMISSION_GRANTED -> Priority.PRIORITY_HIGH_ACCURACY
        else -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
      }
    return fusedLocationClient.getCurrentLocation(priority, CancellationTokenSource().token).await()
  }

  private fun Location.isLocationExpired(
    referenceTimeMillis: Long = SystemClock.elapsedRealtime(),
    expireDuration: Duration = LOCATION_EXPIRE_DURATION,
  ): Boolean {
    return (referenceTimeMillis - LocationCompat.getElapsedRealtimeMillis(this)).milliseconds >
      expireDuration
  }
}
