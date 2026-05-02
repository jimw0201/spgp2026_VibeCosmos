package kr.ac.tukorea.jmw.vibecosmos.game.main

import kr.ac.tukorea.jmw.a2dg.objects.Sprite
import kr.ac.tukorea.jmw.a2dg.view.GameContext
import kr.ac.tukorea.jmw.vibecosmos.R

class Note(gctx: GameContext, val lane: Player.State) : Sprite(gctx, R.mipmap.air1) {
    init {
        val targetY = if (lane == Player.State.UP_ATK) 300f else 500f

        setCenter(1700f, targetY)

        width = 100f
        height = 100f
    }

    override fun update(gctx: GameContext) {
        val elapsedSeconds = gctx.frameTime

        x -= 600 * elapsedSeconds

        syncDstRect()
    }
}