package kr.ac.tukorea.jmw.vibecosmos.game.data

import java.io.Serializable

data class SongConfig(
    val title: String,          // 곡 제목
    val musicResId: Int,        // R.raw.music_file
    val bgResId: Int,           // R.mipmap.background_file
    val chartFileName: String,  // "song_01.txt"
    val noteSpeed: Float        // 곡마다 다른 노트 속도
) : Serializable