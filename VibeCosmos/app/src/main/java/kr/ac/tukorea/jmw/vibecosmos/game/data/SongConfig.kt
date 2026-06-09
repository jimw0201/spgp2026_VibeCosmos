package kr.ac.tukorea.jmw.vibecosmos.game.data

import kotlinx.serialization.Serializable
import android.content.Context

@Serializable
data class SongConfig(
    val title: String,          // 곡 제목
    val musicFileName: String,  // 예: "music1"
    val bgFileName: String,     // 예: "stage_bg1"
    val chartFileName: String,  // "chart1.txt"
    val noteSpeed: Float        // 노트 속도
) : java.io.Serializable {

    fun getMusicResId(context: Context): Int {
        return context.resources.getIdentifier(musicFileName, "raw", context.packageName)
    }

    fun getBgResId(context: Context): Int {
        return context.resources.getIdentifier(bgFileName, "mipmap", context.packageName)
    }
}