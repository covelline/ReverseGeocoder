package com.covelline.reversegeocoder.data

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.nio.file.Path

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class GsiFeatureDatabaseBuilder(
    private val outputDirectory: Path
) {
    /**
     * 与えられたパスのDBファイルを使ってRoom Databaseを作成します
     * */
    actual fun createGsiFeatureDatabase(): GsiFeatureDatabase {
        // JVMは前処理用なので、DBファイルを毎回削除して作り直す
        val dbFile = File(outputDirectory.toFile(), GsiFeatureDatabase.DB_FILE_NAME)
        dbFile.delete()
        val builder = Room
            .databaseBuilder<GsiFeatureDatabase>(
                name = dbFile.absolutePath
            )
        return builder
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }

}