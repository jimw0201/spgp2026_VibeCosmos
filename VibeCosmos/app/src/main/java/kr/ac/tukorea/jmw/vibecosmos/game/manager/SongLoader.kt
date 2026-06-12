package kr.ac.tukorea.jmw.vibecosmos.game.manager

import android.content.res.AssetManager
import kr.ac.tukorea.jmw.vibecosmos.game.data.SongConfig
import org.json.JSONArray

object SongLoader {
    private const val SONGS_JSON = "songs.json"

    fun load(assets: AssetManager): List<SongConfig> {
        val songList = ArrayList<SongConfig>()

        return try {
            val text = assets.open(SONGS_JSON).bufferedReader().use { it.readText() }

            val cleanText = text.trim().removePrefix("\uFEFF")

            val jsonArray = JSONArray(cleanText)

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)

                val title = jsonObject.getString("title")
                val musicFileName = jsonObject.getString("musicFileName")
                val bgFileName = jsonObject.getString("bgFileName")
                val chartFileName = jsonObject.getString("chartFileName")

                val noteSpeed = jsonObject.getDouble("noteSpeed").toFloat()

                val config = SongConfig(title, musicFileName, bgFileName, chartFileName, noteSpeed)
                songList.add(config)
            }

            songList
        } catch (e: Exception) {
            android.util.Log.e("SONG_PARSER_ERROR", "최종 파싱 실패 에러 로그: ${e.localizedMessage}")
            e.printStackTrace()
            emptyList()
        }
    }
}