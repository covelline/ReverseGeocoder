package com.covelline.reversegeocoder.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity
data class GsiMetadata(
    /** データベースのバージョン */
    @PrimaryKey val databaseVersion: Int,
    /** 国土地理院情報のバージョン情報 */
    val gisDataVersionInfo: String,
)

@Dao
interface MetadataDao {
    @Query("SELECT * FROM GsiMetadata LIMIT 1")
    suspend fun getMetadata(): GsiMetadata?

    @Insert(entity = GsiMetadata::class)
    suspend fun insertMetadata(metadata: GsiMetadata)
}
