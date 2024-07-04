package com.covelline.reversegeocoder.ui.screen

import android.Manifest
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.viewmodel.compose.viewModel
import com.covelline.reversegeocoder.BuildConfig
import com.covelline.reversegeocoder.R
import com.covelline.reversegeocoder.ui.theme.ReverseGeocoderTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SettingsScreen(
  modifier: Modifier = Modifier,
  viewModel: CurrentLocationListViewModel = viewModel(),
  onBackClicked: (() -> Unit)? = null,
  onNavigateToLicense: () -> Unit = {},
) {
  val backgroundLocationPermissionState =
    rememberPermissionState(permission = Manifest.permission.ACCESS_BACKGROUND_LOCATION)
  val isBackgroundLocationUpdateEnabled by
    viewModel.isBackgroundLocationUpdateEnabled.collectAsState(false)
  SettingsScreen(
    modifier = modifier,
    hasBackgroundLocationPermission = backgroundLocationPermissionState.status.isGranted,
    isBackgroundLocationUpdateEnabled = isBackgroundLocationUpdateEnabled,
    onNavigateToLicense = onNavigateToLicense,
    onChangeBackgroundLocationUpdateAvailability =
      viewModel::onChangeBackgroundLocationAccessSwitch,
    onBackClicked = onBackClicked,
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
  modifier: Modifier = Modifier,
  hasBackgroundLocationPermission: Boolean = false,
  isBackgroundLocationUpdateEnabled: Boolean = false,
  onNavigateToLicense: () -> Unit = {},
  onChangeBackgroundLocationUpdateAvailability: (Boolean) -> Unit = {},
  onBackClicked: (() -> Unit)? = null,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(text = stringResource(id = R.string.settings_title)) },
        navigationIcon = {
          onBackClicked?.let {
            IconButton(onClick = it) {
              Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
            }
          }
        },
      )
    },
  ) { paddingValues ->
    Column(
      modifier =
        Modifier.padding(paddingValues).fillMaxWidth().verticalScroll(rememberScrollState())
    ) {
      if (hasBackgroundLocationPermission) {
        ChangeBackgroundLocationAvailabilitySwitch(
          isBackgroundLocationUpdateEnabled = isBackgroundLocationUpdateEnabled,
          onChange = onChangeBackgroundLocationUpdateAvailability,
        )
      }
      VersionInformationLabels()
      ListItem(
        modifier = Modifier.clickable { onNavigateToLicense() },
        headlineContent = { Text(text = stringResource(id = R.string.license_title)) },
        trailingContent = {
          Icon(imageVector = Icons.AutoMirrored.Default.ArrowForward, contentDescription = null)
        },
      )
    }
  }
}

@Composable
private fun ChangeBackgroundLocationAvailabilitySwitch(
  modifier: Modifier = Modifier,
  isBackgroundLocationUpdateEnabled: Boolean = false,
  onChange: (Boolean) -> Unit = {},
) {
  ListItem(
    modifier = modifier,
    headlineContent = {
      Text(text = stringResource(id = R.string.settings_change_background_location_access_switch))
    },
    trailingContent = {
      Switch(checked = isBackgroundLocationUpdateEnabled, onCheckedChange = onChange)
    },
  )
}

@Composable
private fun VersionInformationLabels(modifier: Modifier = Modifier) {
  ListItem(
    modifier = modifier,
    headlineContent = { Text(text = stringResource(R.string.settings_version_title)) },
    supportingContent = {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(text = stringResource(R.string.settings_version_application_version_title))
          Text(text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        }
        Text(
          text =
            "国土交通省国土数値情報ダウンロードサイト https://nlftp.mlit.go.jp/ksj/gml/datalist/KsjTmplt-N03-2024.html"
        )
      }
    },
  )
}

@PreviewFontScale
@PreviewLightDark
@Preview(name = "Settings", showSystemUi = true)
@Composable
private fun PreviewSettings() {
  ReverseGeocoderTheme {
    SettingsScreen(
      hasBackgroundLocationPermission = true,
      isBackgroundLocationUpdateEnabled = true,
      onChangeBackgroundLocationUpdateAvailability = {},
    )
  }
}
