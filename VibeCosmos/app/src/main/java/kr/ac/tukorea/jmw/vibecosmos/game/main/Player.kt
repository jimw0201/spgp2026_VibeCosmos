package kr.ac.tukorea.jmw.vibecosmos.game.main

import kr.ac.tukorea.jmw.a2dg.objects.Sprite
import kr.ac.tukorea.jmw.a2dg.view.GameContext
import kr.ac.tukorea.jmw.vibecosmos.R

class Player(gctx: GameContext): Sprite(gctx, R.mipmap.player_run) {
    init {
        setCenterProportionalWidth(200f, 700f, 200f)
    }
}