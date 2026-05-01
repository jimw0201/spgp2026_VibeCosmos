package kr.ac.tukorea.jmw.vibecosmos.game.main

import android.graphics.Canvas
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

    private var stateStartTime = System.currentTimeMillis()

    init {
        width = Player.WIDTH
        height = Player.HEIGHT
        setCenter(200f, 500f)
    }

    var state = State.RUN
        set(value) {
            if (field == value) return
            field = value

            val (resId, frameCount, scale) = when (value) {
                State.RUN -> Triple(R.mipmap.player_run, 32, 1.0f)
                State.UP_ATK -> Triple(R.mipmap.player_up_atk, 30, 1.3f)
                State.DOWN_ATK -> Triple(R.mipmap.player_down_atk, 22, 1.3f)
            }

            bitmap = gctx.res.getBitmap(resId)
            this.frameCount = frameCount

            this.width = WIDTH * scale
            this.height = HEIGHT * scale

            this.stateStartTime = System.currentTimeMillis()
        }

    override fun draw(canvas: Canvas) {
        syncDstRect()

        val elapsedSeconds = (System.currentTimeMillis() - stateStartTime) / 1000f

        val totalDuration = frameCount / fps

        if (state != State.RUN && elapsedSeconds >= totalDuration) {
            state = State.RUN
        }

        val frameIndex = ((elapsedSeconds * fps).toInt()) % frameCount

        val col = frameIndex % columns
        val row = frameIndex / columns

        srcRect?.set(
            col * frameWidth,
            row * frameHeight,
            (col + 1) * frameWidth,
            (row + 1) * frameHeight
        )
        canvas.drawBitmap(bitmap, srcRect, dstRect, null)
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