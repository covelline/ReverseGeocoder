package com.covelline.reversegeocoder.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.covelline.reversegeocoder.data.GsiFeatureDatabase.Companion.VERSION

/** アプリ内で使う地理的特徴を格納するデータベース */
@Database(
    entities = [AdministrativeArea::class, JarlCityWardCountyCode::class, GsiMetadata::class],
    version = VERSION,
    exportSchema = true,
)
abstract class GsiFeatureDatabase : RoomDatabase() {
    companion object {
        const val DB_FILE_NAME = "gsi_feature_database.db"
        const val VERSION = 1
    }
    abstract fun administrativeAreaDao(): AdministrativeAreaDao
    abstract fun metadataDao(): MetadataDao
}
