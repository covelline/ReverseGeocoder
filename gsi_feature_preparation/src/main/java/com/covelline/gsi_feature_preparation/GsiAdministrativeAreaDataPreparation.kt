package com.covelline.gsi_feature_preparation

import com.covelline.reversegeocoder.data.AdministrativeArea
import com.covelline.reversegeocoder.data.GsiFeatureDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.simple.parser.JSONParser
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.io.geojson.GeoJsonReader
import java.io.File
import java.io.FileInputStream

/**
 * 国土地理院が公開している行政区分データをロードし、DBに格納する処理
 * */
class GsiAdministrativeAreaDataPreparation(
    private val gsiDataPath: File,
    private val gsiFeatureDatabase: GsiFeatureDatabase,
) {

    companion object {
        // JARLによるエリア番号が割り当てられていない住所
        val jarlExtractedArea = listOf(
            "所属未定地",
            "蘂取郡蘂取村",
            "色丹郡色丹村",
            "国後郡泊村",
            "国後郡留夜別村",
            "択捉郡留別村",
            "紗那郡紗那村",
        )
    }

    fun proceed() {
        runBlocking {
            loadFeature()
        }
    }

    private suspend fun loadFeature() {
        val dao = gsiFeatureDatabase.administrativeAreaDao()
        withContext(Dispatchers.IO) {
            FileInputStream(gsiDataPath).reader()
        }.use { reader ->
            val parser = JSONParser()
            val geoJsonReader = GeoJsonReader(GeometryFactory())
            val json = parser.parse(reader) as Map<*, *>
            val features = json["features"] as List<*>
            features.forEach { feature ->
                val properties = (feature as Map<*, *>)["properties"] as Map<*, *>
                val prefecture = properties["N03_001"] as String
                val subPrefecture = properties["N03_002"] as String?
                val county = properties["N03_003"] as String? // 郡
                val city = properties["N03_004"] as String? // 市区町村
                val ward = properties["N03_005"] as String? // 区
                val code = properties["N03_007"] as String
                val geometryJson = feature["geometry"].toString()
                val geometry = geoJsonReader.read(geometryJson)
                val minX = geometry.envelopeInternal.minX
                val minY = geometry.envelopeInternal.minY
                val maxX = geometry.envelopeInternal.maxX
                val maxY = geometry.envelopeInternal.maxY
                val address = listOfNotNull(county, city, ward).joinToString("")
                val jarlCityWardCountyCode = if (jarlExtractedArea.contains(address)) {
                    null
                } else {
                    dao.findJarlCityWardCountyCodeId(prefecture, address).run {
                        if (this == null) {
                            println("$prefecture$address の JCC が見つかりませんでした")
                        }
                        this
                    }
                }
                val administrativeArea = AdministrativeArea(
                    prefecture = prefecture,
                    subPrefecture = subPrefecture,
                    county = county,
                    city = city,
                    ward = ward,
                    code = code,
                    polygon = geometryJson,
                    minX = minX,
                    minY = minY,
                    maxX = maxX,
                    maxY = maxY,
                    jarlCityWardCountryCodeId = jarlCityWardCountyCode
                )
                dao.insert(administrativeArea)
            }
        }
    }

}