package com.covelline.reversegeocoder.data

import android.content.Context
import androidx.room.Room

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class GsiFeatureDatabaseBuilder(private val context: Context) {

    actual fun createGsiFeatureDatabase(): GsiFeatureDatabase {
        // アプリ内でデータを新たに書き込むケースは無いので、毎回新しいデータベースを作る
        val dbPath = context.getDatabasePath(GsiFeatureDatabase.DB_FILE_NAME)
        dbPath.delete()

        return Room.databaseBuilder(context, GsiFeatureDatabase::class.java, GsiFeatureDatabase.DB_FILE_NAME)
            .createFromAsset(GsiFeatureDatabase.DB_FILE_NAME)
            .build()
    }

}