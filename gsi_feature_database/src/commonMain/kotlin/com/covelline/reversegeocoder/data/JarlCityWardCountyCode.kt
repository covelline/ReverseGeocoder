package com.covelline.reversegeocoder.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * JARLが定める市郡区番号データ
 * */
@Entity
data class JarlCityWardCountyCode(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    /**
     * JCC
     * */
    val jccNumber: String? = null,
    /**
     * JCG
     * */
    val jcgNumber: String? = null,
    /**
     * 区番号
     * */
    val kuNumber: String? = null,
    val prefecture: String, // 都道府県
    val city: String,  // 市または郡
    val ward: String? // 町または区
) {
    init {
        val codes = listOfNotNull(jccNumber, jcgNumber, kuNumber)
        require(codes.size == 1) { "JCC, JCG, 区番号のいずれか一つだけを指定してください。指定されたデータ: ${codes.joinToString()}" }
    }
}
