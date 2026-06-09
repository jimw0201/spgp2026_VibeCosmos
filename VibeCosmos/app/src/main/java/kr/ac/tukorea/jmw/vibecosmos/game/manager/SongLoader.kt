package kr.ac.tukorea.jmw.vibecosmos.game.manager

import android.content.res.AssetManager
import kotlinx.serialization.json.Json
import kr.ac.tukorea.jmw.vibecosmos.game.data.SongConfig

object SongLoader {
    private const val SONGS_JSON = "songs.json"

    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun load(assets: AssetManager): List<SongConfig> {
        return try {
            val text = assets.open(SONGS_JSON).bufferedReader().use { it.readText() }
            json.decodeFromString<List<SongConfig>>(text)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}