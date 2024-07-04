@file:OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)

package com.covelline.reversegeocoder.ui.screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.covelline.reversegeocoder.FindAdministrativeAreaErrorReason
import com.covelline.reversegeocoder.JarlCityWardCountyCode
import com.covelline.reversegeocoder.JarlCityWardCountyCodeType
import com.covelline.reversegeocoder.LocationData
import com.covelline.reversegeocoder.R
import com.covelline.reversegeocoder.UpdateLocationError
import com.covelline.reversegeocoder.UpdateLocationErrorReason
import com.covelline.reversegeocoder.administrativeArea
import com.covelline.reversegeocoder.findAdministrativeAreaError
import com.covelline.reversegeocoder.jarlCityWardCountyCode
import com.covelline.reversegeocoder.location
import com.covelline.reversegeocoder.locationData
import com.covelline.reversegeocoder.permissions.anyPermissionsGranted
import com.covelline.reversegeocoder.ui.theme.ReverseGeocoderTheme
import com.covelline.reversegeocoder.updateLocationError
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.protobuf.timestamp
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CurrentLocationListScreen(
  modifier: Modifier = Modifier,
  viewModel: CurrentLocationListViewModel = viewModel(),
  navigateToSetting: () -> Unit = {},
) {
  val context = LocalContext.current
  val locationPermissions =
    listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
  val locationPermissionState =
    rememberMultiplePermissionsState(permissions = locationPermissions) { result ->
      // FINEかCOARSEのいずれかが許可されていたら、位置情報は取得可能なので
      // 取得する処理を呼び出す
      if (result.values.any { true }) {
        if (
          ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
              context,
              Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
          viewModel.onLocationPermissionGranted()
        }
      }
    }
  // バックグラウンドの位置情報取得は、ACCESS_FINE/COARSE_LOCATIONとは別で要求しなければならない
  val backgroundLocationPermissionState =
    rememberPermissionState(permission = Manifest.permission.ACCESS_BACKGROUND_LOCATION) { granted
      ->
      // バックグラウンドの位置情報取得機能はユーザーが有効・無効化できるのでパーミッション変化時のみだけ操作してLaunchedEffectでは行わない
      if (granted) {
        viewModel.onBackgroundLocationPermissionGranted()
      } else {
        viewModel.onBackgroundLocationPermissionDenied()
      }
    }

  // パーミッションを求める
  // Stateとして提供されるプロパティが1つでも変化したらパーミッションの設定が切り替わっているので、
  // パーミッションの設定変更をトリガーに画面を更新する
  LaunchedEffect(locationPermissionState.anyPermissionsGranted().value) {
    if (!locationPermissionState.anyPermissionsGranted().value) {
      locationPermissionState.launchMultiplePermissionRequest()
    }
  }
  LaunchedEffect(backgroundLocationPermissionState.status) {
    if (!backgroundLocationPermissionState.status.isGranted) {
      // バックグラウンドの位置情報取得が許可されていない場合は、過去のWorkerを含めて止める
      viewModel.onBackgroundLocationPermissionDenied()
    }
  }

  if (!locationPermissionState.anyPermissionsGranted().value) {
    RequestPermissionScreen(modifier = modifier)
  } else {
    val locationData by viewModel.locationData.collectAsState(LocationData.getDefaultInstance())
    val isBackgroundLocationUpdateRunning by viewModel.isLocationUpdateRunning.collectAsState(false)
    CurrentLocationListScreen(
      modifier = modifier,
      locationData = locationData,
      showRequestBackgroundLocationButton = !backgroundLocationPermissionState.status.isGranted,
      isBackgroundLocationUpdateRunning = isBackgroundLocationUpdateRunning,
      onGetCurrentLocationButtonClicked = viewModel::onGetCurrentLocationButtonClicked,
      onRequestBackgroundLocationButtonClicked = {
        backgroundLocationPermissionState.launchPermissionRequest()
      },
      onSettingsButtonClicked = { navigateToSetting() },
    )
  }
}

@Composable
private fun RequestPermissionScreen(modifier: Modifier = Modifier) {
  val context = LocalContext.current
  Scaffold(modifier = modifier) { paddingValues ->
    Column(
      modifier =
        Modifier.fillMaxWidth().padding(paddingValues).verticalScroll(rememberScrollState())
    ) {
      Spacer(modifier = Modifier.height(56.dp))
      Text(text = stringResource(R.string.request_permission_description))
      Spacer(modifier = Modifier.height(12.dp))
      Button(
        onClick = {
          val intent =
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
              data = Uri.fromParts("package", context.packageName, null)
            }
          context.startActivity(intent)
        }
      ) {
        Text(text = stringResource(R.string.request_permission_open_setting_button))
      }
    }
  }
}

@Composable
private fun CurrentLocationListScreen(
  modifier: Modifier = Modifier,
  locationData: LocationData,
  showRequestBackgroundLocationButton: Boolean,
  isBackgroundLocationUpdateRunning: Boolean = false,
  onSettingsButtonClicked: () -> Unit = {},
  onGetCurrentLocationButtonClicked: () -> Unit = {},
  onRequestBackgroundLocationButtonClicked: () -> Unit = {},
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(text = stringResource(R.string.current_location_list_title)) },
        actions = {
          IconButton(onClick = onSettingsButtonClicked) {
            Icon(
              Icons.Default.Settings,
              contentDescription = stringResource(R.string.settings_title),
            )
          }
        },
      )
    },
    floatingActionButton = {
      ExtendedFloatingActionButton(
        text = {
          Text(
            text = stringResource(id = R.string.current_location_list_get_current_location_button)
          )
        },
        icon = {
          Icon(
            modifier = Modifier.size(24.dp),
            imageVector = ImageVector.vectorResource(id = R.drawable.my_location),
            contentDescription =
              stringResource(id = R.string.current_location_list_get_current_location_button),
          )
        },
        onClick = {
          if (!isBackgroundLocationUpdateRunning) {
            onGetCurrentLocationButtonClicked()
          }
        },
      )
    },
  ) { paddingValues ->
    Column(modifier = Modifier.padding(paddingValues).verticalScroll(rememberScrollState())) {
      AnimatedVisibility(visible = isBackgroundLocationUpdateRunning) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
          CircularProgressIndicator(modifier = Modifier.size(24.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = stringResource(R.string.current_location_list_screen_update_location_text))
        }
      }
      if (showRequestBackgroundLocationButton) {
        RequestBackgroundLocationUpdateListItem(
          onRequestBackgroundLocationButtonClicked = onRequestBackgroundLocationButtonClicked
        )
      }
      if (locationData.hasAdministrativeArea()) {
        val city =
          listOfNotNull(
              locationData.administrativeArea.prefecture,
              locationData.administrativeArea.county,
              locationData.administrativeArea.city,
              locationData.administrativeArea.ward,
            )
            .joinToString("")
        if (locationData.hasJarlCityWardCountyCode()) {
          MyCityListItem(city = city, jarlCityWardCountyCode = locationData.jarlCityWardCountyCode)
        } else {
          MyCityListItem(city = city)
        }
      }
      if (locationData.hasLocation()) {
        MyLocationListItem(
          latitude = locationData.location.latitude,
          longitude = locationData.location.longitude,
        )
        MyAltitudeListItem(altitude = locationData.location.altitude)
      }
      if (locationData.hasUpdateLocationError()) {
        UpdateLocationErrorListItem(error = locationData.updateLocationError)
      }
      if (locationData.hasTimestamp()) {
        val timestamp =
          locationData.timestamp.let { timestamp ->
            val instant = Instant.ofEpochSecond(timestamp.seconds, timestamp.nanos.toLong())
            LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
          }
        LastUpdateTimestampListItem(timestamp = timestamp)
      }
    }
  }
}

/** バックグラウンド更新の権限を要求する */
@Composable
private fun RequestBackgroundLocationUpdateListItem(
  modifier: Modifier = Modifier,
  onRequestBackgroundLocationButtonClicked: () -> Unit = {},
) {
  ListItem(
    modifier = modifier.clickable { onRequestBackgroundLocationButtonClicked() },
    headlineContent = {
      Text(text = stringResource(R.string.current_location_list_request_background_location_button))
    },
    supportingContent = {
      Text(
        text =
          stringResource(R.string.current_location_list_request_background_location_description)
      )
    },
    trailingContent = { Icon(Icons.AutoMirrored.Default.ArrowForward, contentDescription = null) },
  )
}

/** 緯度経度を表示する */
@Composable
private fun MyLocationListItem(modifier: Modifier = Modifier, latitude: Double, longitude: Double) {
  ListItem(
    modifier = modifier,
    leadingContent = {
      Icon(
        modifier = Modifier.size(24.dp),
        imageVector = ImageVector.vectorResource(id = R.drawable.my_location),
        contentDescription =
          stringResource(R.string.description_current_location_screen_my_location),
      )
    },
    headlineContent = {
      Text(
        text =
          stringResource(R.string.current_location_list_my_location, latitude, longitude)
            .trimIndent()
      )
    },
  )
}

/** 高度を表示する */
@Composable
private fun MyAltitudeListItem(modifier: Modifier = Modifier, altitude: Double) {
  ListItem(
    modifier = modifier,
    leadingContent = {
      Icon(
        modifier = Modifier.size(24.dp),
        imageVector = ImageVector.vectorResource(id = R.drawable.altitude),
        contentDescription = stringResource(R.string.description_current_location_screen_altitude),
      )
    },
    headlineContent = {
      Text(text = stringResource(R.string.current_location_list_altitude, altitude).trimIndent())
    },
  )
}

/** 都市を表示する */
@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
@Composable
private fun MyCityListItem(
  modifier: Modifier = Modifier,
  city: String,
  jarlCityWardCountyCode: JarlCityWardCountyCode? = null,
) {
  ListItem(
    modifier = modifier,
    leadingContent = {
      Icon(
        modifier = Modifier.size(24.dp),
        imageVector = ImageVector.vectorResource(id = R.drawable.city),
        contentDescription = stringResource(R.string.description_current_location_screen_city),
      )
    },
    headlineContent = {
      Text(text = stringResource(R.string.current_location_list_address, city).trimIndent())
    },
    supportingContent = {
      if (jarlCityWardCountyCode != null) {
        val codeType =
          when (jarlCityWardCountyCode.type) {
            JarlCityWardCountyCodeType.JCC,
            JarlCityWardCountyCodeType.Ku -> "JCC"
            JarlCityWardCountyCodeType.JCG -> "JCG"
            JarlCityWardCountyCodeType.UNRECOGNIZED -> ""
          }
        Text(text = "$codeType: ${jarlCityWardCountyCode.code}")
      }
    },
  )
}

/** 位置情報取得エラー */
@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
@Composable
private fun UpdateLocationErrorListItem(modifier: Modifier = Modifier, error: UpdateLocationError) {
  ListItem(
    modifier = modifier,
    headlineContent = {
      Text(text = stringResource(R.string.current_location_list_update_location_error_title))
    },
    supportingContent = {
      when (error.reason) {
        UpdateLocationErrorReason.PERMISSION_DENIED ->
          Text(
            text =
              stringResource(
                R.string.current_location_list_screen_permission_denied_error_description
              )
          )
        UpdateLocationErrorReason.API_ERROR ->
          Text(
            text =
              stringResource(
                  R.string.current_location_list_update_location_api_error_description,
                  error.message,
                )
                .trimIndent()
          )
        UpdateLocationErrorReason.UNRECOGNIZED -> {}
      }
    },
  )
}

/** 最終更新時を表示する */
@Composable
private fun LastUpdateTimestampListItem(modifier: Modifier = Modifier, timestamp: LocalDateTime) {
  ListItem(
    modifier = modifier,
    headlineContent = { Text(text = "最終更新日時") },
    supportingContent = {
      Text(
        text =
          timestamp.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
      )
    },
  )
}

@PreviewFontScale
@PreviewLightDark
@Preview(name = "CurrentLocationList", showSystemUi = true)
@Composable
fun PreviewCurrentLocationListScreen() {
  var isBackgroundLocationUpdateRunning by remember { mutableStateOf(false) }
  val coroutineScope = rememberCoroutineScope()
  ReverseGeocoderTheme {
    CurrentLocationListScreen(
      locationData =
        locationData {
          timestamp = timestamp {
            val locationTime = 1630000000.milliseconds.inWholeSeconds
            seconds = locationTime
          }
          location = location {
            latitude = 35.681236
            longitude = 139.767125
            altitude = 0.0
          }
          administrativeArea = administrativeArea {
            prefecture = "東京都"
            city = "千代田区"
          }
          jarlCityWardCountyCode = jarlCityWardCountyCode {
            type = JarlCityWardCountyCodeType.JCC
            code = "11011"
          }
        },
      showRequestBackgroundLocationButton = false,
      isBackgroundLocationUpdateRunning = isBackgroundLocationUpdateRunning,
      onGetCurrentLocationButtonClicked = {
        coroutineScope.launch {
          isBackgroundLocationUpdateRunning = true
          delay(5.seconds)
          isBackgroundLocationUpdateRunning = false
        }
      },
    )
  }
}

@PreviewFontScale
@PreviewLightDark
@Preview(name = "CurrentLocationListWithError", showSystemUi = true)
@Composable
fun PreviewCurrentLocationListScreenWithError() {
  ReverseGeocoderTheme {
    CurrentLocationListScreen(
      locationData =
        locationData {
          timestamp = timestamp {
            val locationTime = 1630000000.milliseconds.inWholeSeconds
            seconds = locationTime
          }
          updateLocationError = updateLocationError {
            reason = UpdateLocationErrorReason.API_ERROR
            message = "Some error"
          }
          findAdministrativeAreaError = findAdministrativeAreaError {
            reason = FindAdministrativeAreaErrorReason.NOT_FOUND_ADMINISTRATIVE_AREA
          }
        },
      showRequestBackgroundLocationButton = true,
      isBackgroundLocationUpdateRunning = true,
    )
  }
}
