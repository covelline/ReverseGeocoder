package com.covelline.reversegeocoder.ui.widget

import android.content.Context
import android.graphics.drawable.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.covelline.reversegeocoder.JarlCityWardCountyCode
import com.covelline.reversegeocoder.JarlCityWardCountyCodeType
import com.covelline.reversegeocoder.LocationData
import com.covelline.reversegeocoder.R
import com.covelline.reversegeocoder.background.LocationUpdateWorker
import com.covelline.reversegeocoder.di.DataStoreModule
import com.covelline.reversegeocoder.ui.theme.ReverseGeocoderWidgetTheme
import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
class CurrentLocationListWidget : GlanceAppWidget() {

  companion object {
    private val SMALL_SQUARE = DpSize(129.dp, 50.dp)
    private val LARGE_SQUARE = DpSize(150.dp, 100.dp)
  }

  override val sizeMode: SizeMode = SizeMode.Responsive(setOf(SMALL_SQUARE, LARGE_SQUARE))

  override suspend fun provideGlance(context: Context, id: GlanceId) {
    val locationDataStore = DataStoreModule.providesLocationDataStore(context)
    provideContent {
      // collectAsState でデータが更新されても画面の更新は行われないため first で明示的に
      // 保存されているデータを取得する
      val initialData = runBlocking { locationDataStore.data.first() }
      val locationData by locationDataStore.data.collectAsState(initial = initialData)
      ReverseGeocoderWidgetTheme {
        Scaffold {
          Box(
            modifier = GlanceModifier.fillMaxSize().padding(8.dp),
            contentAlignment = Alignment.BottomEnd,
          ) {
            LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
              if (locationData.hasJarlCityWardCountyCode()) {
                item { JarlCodeLabel(jarlCityWardCountyCode = locationData.jarlCityWardCountyCode) }
              }
              if (locationData.hasAdministrativeArea()) {
                item { AddressLabel(locationData = locationData) }
              }
              if (locationData.hasLocation()) {
                item { LocationLabels(locationData = locationData) }
                item { UpdateTimestampLabel(locationData = locationData) }
              }
              // 現在位置更新ボタン用のpadding
              item { Spacer(modifier = GlanceModifier.height(40.dp)) }
            }
            Box(
              modifier =
                GlanceModifier.size(40.dp)
                  .background(GlanceTheme.colors.primaryContainer)
                  .clickable {
                    WorkManager.getInstance(context)
                      .enqueue(OneTimeWorkRequestBuilder<LocationUpdateWorker>().build())
                  },
              contentAlignment = Alignment.Center,
            ) {
              Image(
                modifier = GlanceModifier.size(24.dp),
                provider = ImageProvider(Icon.createWithResource(context, R.drawable.my_location)),
                contentDescription =
                  context.getString(R.string.current_location_list_get_current_location_button),
              )
            }
          }
        }
      }
    }
  }

  /** 位置と高度を表示する */
  @Composable
  fun LocationLabels(modifier: GlanceModifier = GlanceModifier, locationData: LocationData) {
    val context = LocalContext.current
    val formatter = DecimalFormat("#.###")
    Column(modifier = modifier) {
      Row(modifier = GlanceModifier, verticalAlignment = Alignment.CenterVertically) {
        Image(
          modifier = GlanceModifier.size(24.dp),
          provider = ImageProvider(Icon.createWithResource(context, R.drawable.my_location)),
          contentDescription =
            context.getString(R.string.description_current_location_screen_my_location),
          colorFilter = ColorFilter.tint(GlanceTheme.colors.primary),
        )
        Spacer(modifier = GlanceModifier.width(4.dp))
        Text(
          text =
            """
              ${formatter.format(locationData.location.latitude)}
              ${formatter.format(locationData.location.longitude)}
            """
              .trimIndent(),
          style = TextStyle(color = GlanceTheme.colors.onBackground, fontSize = 24.sp),
        )
      }
      Row(modifier = GlanceModifier, verticalAlignment = Alignment.CenterVertically) {
        Image(
          modifier = GlanceModifier.size(24.dp),
          provider = ImageProvider(Icon.createWithResource(context, R.drawable.altitude)),
          contentDescription =
            context.getString(R.string.description_current_location_screen_altitude),
          colorFilter = ColorFilter.tint(GlanceTheme.colors.primary),
        )
        Spacer(modifier = GlanceModifier.width(4.dp))
        Text(
          text = formatter.format(locationData.location.altitude),
          style = TextStyle(color = GlanceTheme.colors.onBackground, fontSize = 24.sp),
        )
      }
    }
  }

  /** JARLのJCC,JCG,区番号を表示する */
  @Composable
  fun JarlCodeLabel(
    modifier: GlanceModifier = GlanceModifier,
    jarlCityWardCountyCode: JarlCityWardCountyCode,
  ) {
    val codeType =
      when (jarlCityWardCountyCode.type) {
        JarlCityWardCountyCodeType.JCC,
        JarlCityWardCountyCodeType.Ku -> "JCC"
        JarlCityWardCountyCodeType.JCG -> "JCG"
        JarlCityWardCountyCodeType.UNRECOGNIZED -> ""
      }
    Text(
      text = "${codeType}: ${jarlCityWardCountyCode.code}",
      style = TextStyle(color = GlanceTheme.colors.onBackground, fontSize = 24.sp),
    )
  }

  /** 自治体名を表示する 横幅が狭いときは都道府県名 + 最後にマッチした名前 それ以外は全てを表示 最大2行 */
  @Composable
  fun AddressLabel(modifier: GlanceModifier = GlanceModifier, locationData: LocationData) {
    val context = LocalContext.current
    val size = LocalSize.current
    val address =
      when {
        size.width == SMALL_SQUARE.width -> {
          val city =
            listOfNotNull(
                locationData.administrativeArea.county,
                locationData.administrativeArea.city,
                locationData.administrativeArea.ward,
              )
              .first { it.isNotBlank() }
          "${locationData.administrativeArea.prefecture}$city"
        }
        else -> {
          listOfNotNull(
              locationData.administrativeArea.prefecture,
              locationData.administrativeArea.county,
              locationData.administrativeArea.city,
              locationData.administrativeArea.ward,
            )
            .joinToString(separator = "")
        }
      }
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
      Image(
        modifier = GlanceModifier.size(24.dp),
        provider = ImageProvider(Icon.createWithResource(context, R.drawable.city)),
        contentDescription = context.getString(R.string.description_current_location_screen_city),
        colorFilter = ColorFilter.tint(GlanceTheme.colors.primary),
      )
      Spacer(modifier = GlanceModifier.width(4.dp))
      Text(
        text = address,
        maxLines = 2,
        style = TextStyle(color = GlanceTheme.colors.onBackground, fontSize = 24.sp),
      )
    }
  }

  /** 更新時間を表示。横幅が狭いときは時間だけ */
  @Composable
  fun UpdateTimestampLabel(modifier: GlanceModifier = GlanceModifier, locationData: LocationData) {
    val timestamp =
      locationData.timestamp.let { timestamp ->
        val instant = Instant.ofEpochSecond(timestamp.seconds, timestamp.nanos.toLong())
        LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
      }
    val size = LocalSize.current
    val format =
      when {
        size.width == SMALL_SQUARE.width -> {
          DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)
        }
        else -> DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
      }
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
      Text(
        text = format.format(timestamp),
        style = TextStyle(color = GlanceTheme.colors.tertiary, fontSize = 12.sp),
      )
    }
  }
}
