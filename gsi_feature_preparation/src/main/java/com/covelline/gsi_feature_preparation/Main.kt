package com.covelline.gsi_feature_preparation

import com.covelline.reversegeocoder.data.GsiFeatureDatabase
import com.covelline.reversegeocoder.data.GsiFeatureDatabaseBuilder
import com.covelline.reversegeocoder.data.GsiMetadata
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.Properties
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.exists

fun main() = runBlocking {
    val properties = Properties()
    ClassLoader.getSystemClassLoader().getResourceAsStream("config.properties").use { properties.load(it) }

    val inputFile = File(properties.getProperty("inputFilePath"))
    require(inputFile.exists())
    val awardFile = File(properties.getProperty("awardFilePath"))
    require(awardFile.exists())
    val outputDirectory = Path(properties.getProperty("outputDirectoryPath"))
    require(outputDirectory.exists())

    val gsiFeatureDatabase = GsiFeatureDatabaseBuilder(outputDirectory.absolute()).createGsiFeatureDatabase()
    val jarlCityWardCountryCodeDataPreparation = JarlCityWardCountryCodeDataPreparation(awardFile, gsiFeatureDatabase)
    jarlCityWardCountryCodeDataPreparation.proceed()
    val gsiAdministrativeAreaDataPreparation = GsiAdministrativeAreaDataPreparation(inputFile, gsiFeatureDatabase)
    gsiAdministrativeAreaDataPreparation.proceed()

    gsiFeatureDatabase
        .metadataDao()
        .insertMetadata(
            GsiMetadata(
                databaseVersion = GsiFeatureDatabase.VERSION,
                gisDataVersionInfo = "2024年（令和6年）版 https://nlftp.mlit.go.jp/ksj/gml/datalist/KsjTmplt-N03-2024.html"
            )
        )
}