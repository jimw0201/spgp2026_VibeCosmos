package kr.ac.tukorea.jmw.vibecosmos.app

import kr.ac.tukorea.jmw.a2dg.activity.BaseGameActivity
import kr.ac.tukorea.jmw.a2dg.scene.Scene
import kr.ac.tukorea.jmw.a2dg.view.GameContext
import kr.ac.tukorea.jmw.vibecosmos.game.main.MainScene

class VibeCosmosActivity : BaseGameActivity() {
    override val drawsDebugGrid: Boolean = true
    override val drawsDebugInfo: Boolean = true
    override fun createRootScene(gctx: GameContext): Scene {
        return MainScene(gctx)
    }
}