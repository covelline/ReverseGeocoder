package com.covelline.reversegeocoder.data

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import platform.Foundation.NSFileManager

/**
 * @param databasePath アプリ内データベースのパス
 * @param originalDbPath OnDemand Resourceから取得したデータベースのパス
 * */
@OptIn(ExperimentalForeignApi::class)
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class GsiFeatureDatabaseBuilder(
    private val databasePath: String,
    private val originalDbPath: String
) {

    actual fun createGsiFeatureDatabase(): GsiFeatureDatabase {
        // アプリ内でデータを新たに書き込むケースは無いので、毎回新しいデータベースを作る
        val fileManager = NSFileManager.defaultManager
        fileManager.copyItemAtPath(originalDbPath, databasePath, null)
        return Room
            .databaseBuilder<GsiFeatureDatabase>(
                name = databasePath,
                factory = { GsiFeatureDatabase::class.instantiateImpl() }
            )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()

    }

}
