package kr.ac.tukorea.jmw.vibecosmos.app

import android.os.Build
import kr.ac.tukorea.jmw.a2dg.activity.BaseGameActivity
import kr.ac.tukorea.jmw.a2dg.scene.Scene
import kr.ac.tukorea.jmw.a2dg.view.GameContext
import kr.ac.tukorea.jmw.vibecosmos.BuildConfig
import kr.ac.tukorea.jmw.vibecosmos.R
import kr.ac.tukorea.jmw.vibecosmos.game.data.SongConfig
import kr.ac.tukorea.jmw.vibecosmos.game.main.MainScene

class VibeCosmosActivity : BaseGameActivity() {
    override val drawsDebugGrid: Boolean = BuildConfig.DEBUG
    override val drawsDebugInfo: Boolean = BuildConfig.DEBUG
    override val drawsFpsGraph: Boolean = BuildConfig.DEBUG

    override fun createRootScene(gctx: GameContext): Scene {
        val config = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("SELECTED_SONG", SongConfig::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("SELECTED_SONG") as? SongConfig
        } ?: SongConfig("Default", "title", "stage_bg1", "default.txt", 600f)

        gctx.metrics.setSize(1600f, 900f)

        return MainScene(gctx, config)
    }
}