package kr.ac.tukorea.jmw.vibecosmos.game.manager

import android.content.res.AssetManager
import kr.ac.tukorea.jmw.vibecosmos.game.data.SongConfig

object SongCatalog {
    var songs: List<SongConfig> = emptyList()
        private set

    fun load(assets: AssetManager) {
        songs = SongLoader.load(assets)
    }
}