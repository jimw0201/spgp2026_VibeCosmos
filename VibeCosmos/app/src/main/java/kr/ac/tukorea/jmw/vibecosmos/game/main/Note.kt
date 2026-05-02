package kr.ac.tukorea.jmw.vibecosmos.game.main

import android.graphics.RectF
import kr.ac.tukorea.jmw.a2dg.objects.IBoxCollidable
import kr.ac.tukorea.jmw.a2dg.objects.IRecyclable
import kr.ac.tukorea.jmw.a2dg.objects.Sprite
import kr.ac.tukorea.jmw.a2dg.view.GameContext
import kr.ac.tukorea.jmw.vibecosmos.R

class Note(gctx: GameContext) : Sprite(gctx, R.mipmap.air1), IRecyclable, IBoxCollidable {
    var lane: Player.State = Player.State.RUN

    override val collisionRect: RectF get() = dstRect

    override fun onRecycle() {

    }

    init {
        width = 100f
        height = 100f
    }

    fun reset(lane: Player.State) {
        this.lane = lane
        val targetY = if (lane == Player.State.UP_ATK) 300f else 500f

        setCenter(1700f, targetY)
        syncDstRect()
    }

    override fun update(gctx: GameContext) {
        val elapsedSeconds = gctx.frameTime
        x -= 600 * elapsedSeconds
        syncDstRect()
    }
}