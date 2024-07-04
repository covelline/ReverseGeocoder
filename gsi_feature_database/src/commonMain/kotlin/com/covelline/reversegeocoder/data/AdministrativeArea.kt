package com.covelline.reversegeocoder.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

/** 行政区分データを格納するテーブル ポリゴンのminX, minY, maxX, maxYを格納することで、クエリの実行速度を向上させる */
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = JarlCityWardCountyCode::class,
            parentColumns = ["id"],
            childColumns = ["jarlCityWardCountryCodeId"],
            onDelete = ForeignKey.SET_NULL,
        )
    ],
    indices = [Index("jarlCityWardCountryCodeId")]
)
data class AdministrativeArea(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val prefecture: String,
    val subPrefecture: String?,
    val county: String?,
    val city: String?,
    val ward: String?,
    val code: String,
    val polygon: String,
    val minX: Double,
    val minY: Double,
    val maxX: Double,
    val maxY: Double,
    val jarlCityWardCountryCodeId: Int?,
)

/** 行政区分データにアクセスをするためのDao */
@Dao
interface AdministrativeAreaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(areas: AdministrativeArea)

    @Query(
        """
        SELECT * 
        FROM AdministrativeArea
        WHERE minX <= :longitude AND maxX >= :longitude AND minY <= :latitude AND maxY >= :latitude
    """
    )
    suspend fun getAdministrativeAreasInBounds(
        latitude: Double,
        longitude: Double,
    ): List<AdministrativeArea>

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertJarlCityWardCountyCode(jarlCityWardCountyCode: JarlCityWardCountyCode): Long

    @Query(
        """
        SELECT id 
        FROM JarlCityWardCountyCode 
        WHERE REPLACE(prefecture || IFNULL(city, '') || IFNULL(ward, ''), 'ヶ', 'ケ') = :prefecture || :address
         OR REPLACE(prefecture || IFNULL(city, '') || IFNULL(ward, ''), 'ケ', 'ヶ') = :prefecture || :address
        """
    )
    suspend fun findJarlCityWardCountyCodeId(
        prefecture: String,
        address: String
    ): Int?

    @Query(
        """
            SELECT * FROM JarlCityWardCountyCode WHERE id = :id
        """
    )
    suspend fun getJarlCityWardCountyCode(id: Int): JarlCityWardCountyCode

}
