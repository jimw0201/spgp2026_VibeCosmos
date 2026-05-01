package kr.ac.tukorea.jmw.vibecosmos.game.main

import kr.ac.tukorea.jmw.a2dg.scene.Scene
import kr.ac.tukorea.jmw.a2dg.view.GameContext
import kr.ac.tukorea.jmw.a2dg.objects.HorzScrollBackground
import kr.ac.tukorea.jmw.a2dg.objects.Sprite
import kr.ac.tukorea.jmw.a2dg.scene.World
import kr.ac.tukorea.jmw.vibecosmos.R


class MainScene(gctx: GameContext) : Scene(gctx) {
    override val clipsRect = true
    override val world = World(arrayOf(0)).apply {
        add(HorzScrollBackground(gctx, R.mipmap.stage_bg, -150f), 0)
    }
}