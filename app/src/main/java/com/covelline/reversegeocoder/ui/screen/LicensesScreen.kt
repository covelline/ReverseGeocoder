package com.covelline.reversegeocoder.ui.screen

import android.webkit.WebView
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.covelline.reversegeocoder.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(modifier: Modifier = Modifier, onBackClicked: (() -> Unit)? = null) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(text = stringResource(R.string.license_title)) },
        navigationIcon = {
          onBackClicked?.let {
            IconButton(onClick = it) {
              Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
            }
          }
        },
      )
    },
  ) { paddingValues ->
    AndroidView(
      modifier = Modifier.padding(paddingValues),
      factory = { context ->
        val webView = WebView(context)
        webView.loadUrl("file:///android_asset/licenses.html")
        webView
      },
    )
  }
}
