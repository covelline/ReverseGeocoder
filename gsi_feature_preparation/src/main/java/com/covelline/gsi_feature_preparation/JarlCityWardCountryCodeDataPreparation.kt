package com.covelline.gsi_feature_preparation

import com.covelline.reversegeocoder.data.GsiFeatureDatabase
import com.covelline.reversegeocoder.data.JarlCityWardCountyCode
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.charset.Charset

/**
 * JARLの市郡区番号データを準備する処理
 * */
class JarlCityWardCountryCodeDataPreparation(
    private val awardFile: File,
    private val gsiFeatureDatabase: GsiFeatureDatabase
) {

    companion object {
        // 区がある都道府県リスト
        // エリア番号と都道府県名のマップ
        val PREFECTURES_WITH_KU = mapOf(
            "01" to "北海道",
            "06" to "宮城県",
            "08" to "新潟県",
            "11" to "神奈川県",
            "12" to "千葉県",
            "13" to "埼玉県",
            "18" to "静岡県",
            "20" to "愛知県",
            "22" to "京都府",
            "25" to "大阪府",
            "27" to "兵庫県",
            "31" to "岡山県",
            "35" to "広島県",
            "40" to "福岡県",
            "43" to "熊本県",
        )
    }

    fun proceed() {
        runBlocking {
            val list = parseJccList()
            val dao = gsiFeatureDatabase.administrativeAreaDao()
            list.forEach {
                dao.insertJarlCityWardCountyCode(it)
            }
        }
    }


    // JCCリストを取得する
    @Suppress("NewApi")
    private fun parseJccList(): List<JarlCityWardCountyCode> {
        val addressPattern = Regex("^([^都道府県]+[道都県府])(.*[市郡庁区])(.*)$")
        val addressPatternWithKu = Regex("^(.*市)(.*区)$")
        val targetLines = awardFile.readLines(Charset.forName("Shift_JIS"))
            .drop(1)
            .filter { !it.contains("消滅") }
            .toList()

        val jccList = targetLines.map {
            val line = it
                .replace(Regex("\\(.*?\\)"), "")
                .replace("三瀦郡", "三潴郡")// 旧字体表記揺れ対応
            val columns = line.split(",")
            val qth = columns[2].trim()
            val address = columns[3].trim()
            var jccCode: String? = null
            var jcgCode: String? = null
            var kuCode: String? = null
            // コード取得。4桁ならJCC、最後の1文字がアルファベットならJCG、それ以外は区番号
            when {
                qth.length == 4 -> jccCode = qth
                qth.matches(Regex(".*[a-zA-Z]$")) -> jcgCode = qth
                else -> kuCode = qth
            }
            val prefecture: String
            val city: String
            val word: String?
            when {
                // 丹波篠山市だけはなぜか都道府県名が含まれていないので特殊対応
                address == "丹波篠山市" -> {
                    prefecture = "兵庫県"
                    city = address
                    jccCode = "2731"
                    word = ""
                }
                // 市区町村郡の処理。東京だけは「東京都」が含まれるので、市扱い
                kuCode == null || address.startsWith("東京") -> {
                    val addressGroup = requireNotNull(addressPattern.matchEntire(address)) {
                        "JCCリストの取得に失敗しました。line: $line"
                    }
                    prefecture = addressGroup.groupValues[1]
                    city = addressGroup.groupValues[2].let { c ->
                        // 国土地理院のデータから「支庁」が無くなったので消す
                        if (c.endsWith("支庁")) {
                            ""
                        } else {
                            c
                        }
                    }
                    word = addressGroup.groupValues.getOrNull(3)
                }
                // 区の処理。都道府県名が含まれていないので、エリア番号テーブルから取得する
                else -> {
                    val addressGroup = requireNotNull(addressPatternWithKu.matchEntire(address)) {
                        "JCCリストの取得に失敗しました。line: $line"
                    }
                    prefecture = requireNotNull(PREFECTURES_WITH_KU[kuCode.substring(0, 2)]) {
                        "都道府県名が見つかりませんでした line : $line"
                    }
                    city = addressGroup.groupValues[1]
                    word = addressGroup.groupValues[2]
                }
            }
            JarlCityWardCountyCode(
                jccNumber = jccCode,
                jcgNumber = jcgCode,
                kuNumber = kuCode,
                prefecture = prefecture,
                city = city,
                ward = word
            )
        }.toList()

        require(jccList.size == targetLines.size) {
            "JCCリストの取得に失敗しました。取得数: ${jccList.size}, 全体数: ${targetLines.count()}"
        }
        return jccList
    }

}
