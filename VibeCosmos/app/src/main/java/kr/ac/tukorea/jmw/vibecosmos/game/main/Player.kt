package kr.ac.tukorea.jmw.vibecosmos.game.main

import kr.ac.tukorea.jmw.a2dg.objects.AnimSprite
import kr.ac.tukorea.jmw.a2dg.view.GameContext
import kr.ac.tukorea.jmw.vibecosmos.R

class Player(gctx: GameContext): AnimSprite(gctx, R.mipmap.player_run, 10f) {
    init {
        width = Player.WIDTH
        height = Player.HEIGHT
        setCenter(200f, 700f)
    }

    companion object {
        const val WIDTH = 180f
        const val HEIGHT = 200f
    }
}