package com.covelline.reversegeocoder.permissions

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionStatus

@OptIn(ExperimentalPermissionsApi::class)
/** 1つでもパーミッションが許可されているか */
fun MultiplePermissionsState.anyPermissionsGranted(): State<Boolean> {
  return derivedStateOf { permissions.any { it.status == PermissionStatus.Granted } }
}
