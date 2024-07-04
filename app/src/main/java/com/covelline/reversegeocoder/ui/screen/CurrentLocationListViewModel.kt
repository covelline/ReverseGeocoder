package com.covelline.reversegeocoder.ui.screen

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.covelline.reversegeocoder.LocationData
import com.covelline.reversegeocoder.background.LocationUpdateWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration
import kotlinx.coroutines.flow.map

@HiltViewModel
class CurrentLocationListViewModel
@Inject
constructor(locationDataStore: DataStore<LocationData>, private val workManager: WorkManager) :
  ViewModel() {
  // 位置情報データ
  val locationData = locationDataStore.data
  // 位置情報取得処理実行中かどうか
  val isLocationUpdateRunning =
    workManager
      .getWorkInfosForUniqueWorkFlow(LocationUpdateWorker::class.java.simpleName + "onetime")
      .map { workInfos -> workInfos.any { it.state == WorkInfo.State.RUNNING } }
  // バックグラウンド位置情報取得処理実行中かどうか
  val isBackgroundLocationUpdateEnabled =
    workManager.getWorkInfosForUniqueWorkFlow(LocationUpdateWorker::class.java.simpleName).map {
      workInfo ->
      workInfo.any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING }
    }

  @RequiresPermission(
    anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION]
  )
  // 位置情報パーミッションが許可されたら位置情報の取得を試みる
  fun onLocationPermissionGranted() {
    val locationUpdateRequest = OneTimeWorkRequestBuilder<LocationUpdateWorker>().build()
    workManager.enqueueUniqueWork(
      LocationUpdateWorker::class.java.simpleName + "onetime",
      ExistingWorkPolicy.REPLACE,
      locationUpdateRequest,
    )
  }

  @RequiresPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
  // バックグラウンドで位置情報アクセスが許可された場合に、定期的な位置情報の更新をする
  fun onBackgroundLocationPermissionGranted() {
    val request =
      PeriodicWorkRequestBuilder<LocationUpdateWorker>(15.minutes.toJavaDuration()).build()
    workManager.enqueueUniquePeriodicWork(
      LocationUpdateWorker::class.java.simpleName,
      ExistingPeriodicWorkPolicy.UPDATE,
      request,
    )
  }

  fun onBackgroundLocationPermissionDenied() {
    workManager.cancelUniqueWork(LocationUpdateWorker::class.java.simpleName)
  }

  @RequiresPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
  fun onChangeBackgroundLocationAccessSwitch(value: Boolean) {
    if (value) {
      onBackgroundLocationPermissionGranted()
    } else {
      onBackgroundLocationPermissionDenied()
    }
  }

  @RequiresPermission(
    anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION]
  )
  // 取得した位置情報の内容にユーザーが満足せず、位置情報取得ボタンを押したときに位置情報の更新を強制する
  fun onGetCurrentLocationButtonClicked() {
    val request =
      OneTimeWorkRequestBuilder<LocationUpdateWorker>()
        .setInputData(
          Data.Builder().putBoolean(LocationUpdateWorker.FORCE_UPDATE_KEY, true).build()
        )
        .build()
    workManager.enqueue(request)
  }
}
