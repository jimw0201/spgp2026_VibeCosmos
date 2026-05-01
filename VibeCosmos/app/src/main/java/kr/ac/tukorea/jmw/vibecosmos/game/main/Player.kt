package kr.ac.tukorea.jmw.vibecosmos.game.main

import kr.ac.tukorea.jmw.a2dg.objects.AnimSprite
import kr.ac.tukorea.jmw.a2dg.view.GameContext
import kr.ac.tukorea.jmw.vibecosmos.R

class Player(gctx: GameContext): AnimSprite(
    gctx,
    R.mipmap.player_run,
    60f,
    frameCount = 32,
    columns = 8
    ) {
    enum class State {
        RUN, UP_ATK, DOWN_ATK,
    }

    init {
        width = Player.WIDTH
        height = Player.HEIGHT
        setCenter(200f, 500f)
    }

    var state = State.RUN
        set(value) {
            val (resId, frameCount) = when (value) {
                State.RUN -> R.mipmap.player_run to 32
                State.UP_ATK -> R.mipmap.player_up_atk to 30
                State.DOWN_ATK -> R.mipmap.player_down_atk to 22
            }

            bitmap = gctx.res.getBitmap(resId)
            this.frameCount = frameCount
        }

    fun attackUp() {
        state = State.UP_ATK
    }

    fun attackDown() {
        state = State.DOWN_ATK
    }

    companion object {
        const val WIDTH = 200f
        const val HEIGHT = 261f
    }
}