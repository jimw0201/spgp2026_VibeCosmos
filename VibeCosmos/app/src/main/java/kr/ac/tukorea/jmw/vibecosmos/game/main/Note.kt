package kr.ac.tukorea.jmw.vibecosmos.game.main

import android.graphics.RectF
import kr.ac.tukorea.jmw.a2dg.objects.IBoxCollidable
import kr.ac.tukorea.jmw.a2dg.objects.IRecyclable
import kr.ac.tukorea.jmw.a2dg.objects.Sprite
import kr.ac.tukorea.jmw.a2dg.view.GameContext
import kr.ac.tukorea.jmw.vibecosmos.R

class Note(gctx: GameContext) : Sprite(gctx, R.mipmap.air1), IRecyclable, IBoxCollidable {
    lateinit var lane: Player.State
    private var speed: Float = 0f

    var lengthMs: Long = 0L
    val isLongNote: Boolean get() = lengthMs > 0L

    // 베지에 제어점 초기화
    private var p0X = 1600f
    private var p0Y = 0f
    private var p1X = 1400f
    private var p1Y = 0f
    private var p2X = 1200f
    private var p2Y = 0f

    private var progress = 0f
    private var interpolationSpeed = 0.5f

    private val fixedCollisionRect = RectF()

    override val collisionRect: RectF
        get() {
            fixedCollisionRect.set(
                x - width / 2f,
                y - height / 2f,
                x + width / 2f,
                y + height / 2f
            )
            return fixedCollisionRect
        }

    fun reset(lane: Player.State, speed: Float, lengthMs: Long = 0L) {
        this.lane = lane
        this.speed = speed
        this.progress = 0f
        this.lengthMs = lengthMs

        val targetLaneY = if (lane == Player.State.UP_ATK) 300f else 500f

        p0X = 1600f; p1X = 1400f; p2X = 1200f
        p2Y = targetLaneY; p1Y = targetLaneY

        if (lane == Player.State.UP_ATK) {
            p0Y = -50f
        } else {
            p0Y = 950f
        }

        interpolationSpeed = speed / (p0X - p2X)

        val bitmapWidth = bitmap?.width ?: 100
        val bitmapHeight = bitmap?.height ?: 100

        val desiredWidth = 140f
        val aspectRatio = bitmapHeight.toFloat() / bitmapWidth.toFloat()
        val desiredHeight = desiredWidth * aspectRatio

        setSize(desiredWidth, desiredHeight)
        setCenter(p0X, p0Y)
    }

    override fun update(gctx: GameContext) {
        if (progress < 1f) {
            progress += gctx.frameTime * interpolationSpeed
            if (progress > 1f) progress = 1f

            val t = progress
            val oneMinusT = 1f - t

            val bezierX = (oneMinusT * oneMinusT * p0X) + (2f * oneMinusT * t * p1X) + (t * t * p2X)
            val bezierY = (oneMinusT * oneMinusT * p0Y) + (2f * oneMinusT * t * p1Y) + (t * t * p2Y)

            setCenter(bezierX, bezierY)
        } else {
            x -= speed * gctx.frameTime
            y = p2Y
        }

        syncDstRect()
    }

    override fun onRecycle() {
        progress = 0f
        lengthMs = 0L
    }
}