package kr.ac.tukorea.jmw.vibecosmos.game.main

import kr.ac.tukorea.jmw.a2dg.scene.Scene
import kr.ac.tukorea.jmw.a2dg.view.GameContext
import kr.ac.tukorea.jmw.a2dg.objects.HorzScrollBackground
import kr.ac.tukorea.jmw.a2dg.scene.World
import kr.ac.tukorea.jmw.vibecosmos.R


class MainScene(gctx: GameContext) : Scene(gctx) {
    enum class Layer {
        BG, PLAYER
    }
    override val clipsRect = true
    override val world = World(Layer.entries.toTypedArray()).apply {
        listOf(
            R.mipmap.stage_bg to -150f,
        ).forEach { (resId, speed) ->
            add(HorzScrollBackground(gctx, resId, speed), Layer.BG)
        }
        add(Player(gctx), Layer.PLAYER)
    }
}