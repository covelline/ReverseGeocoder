package com.covelline.reversegeocoder.data

import androidx.datastore.core.Serializer
import com.covelline.reversegeocoder.LocationData
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import timber.log.Timber

object LocationDataSerializer : Serializer<LocationData> {
  override val defaultValue: LocationData = LocationData.getDefaultInstance()

  override suspend fun readFrom(input: InputStream): LocationData {
    try {
      return LocationData.parseFrom(input)
    } catch (e: IOException) {
      Timber.e(e)
      throw e
    }
  }

  override suspend fun writeTo(t: LocationData, output: OutputStream) {
    try {
      t.writeTo(output)
    } catch (e: Exception) {
      Timber.e(e)
      throw IOException("Failed to write LocationData.", e)
    }
  }
}
