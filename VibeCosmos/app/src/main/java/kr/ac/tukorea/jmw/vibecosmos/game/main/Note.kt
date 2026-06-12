package kr.ac.tukorea.jmw.vibecosmos.game.main

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import kr.ac.tukorea.jmw.a2dg.objects.IBoxCollidable
import kr.ac.tukorea.jmw.a2dg.objects.IRecyclable
import kr.ac.tukorea.jmw.a2dg.objects.Sprite
import kr.ac.tukorea.jmw.a2dg.view.GameContext
import kr.ac.tukorea.jmw.vibecosmos.R

class Note(gctx: GameContext) : Sprite(gctx, R.mipmap.air1), IRecyclable, IBoxCollidable {
    lateinit var lane: Player.State
    var speed: Float = 0f
        private set

    var lengthMs: Long = 0L
    val isLongNote: Boolean get() = lengthMs > 0L

    var isHolding: Boolean = false

    private val alphaPaint = Paint()

    private var headBitmap: Bitmap? = null
    private var lineBitmap: Bitmap? = null
    private var tailBitmap: Bitmap? = null

    // 베지에 제어점 초기화
    private var p0X = 1600f
    private var p0Y = 0f
    private var p1X = 1400f
    private var p1Y = 0f
    private var p2X = 1200f
    private var p2Y = 0f

    private var progress = 0f
    private var interpolationSpeed = 0.5f

    private val componentRect = RectF()
    private val fixedCollisionRect = RectF()

    init {
        headBitmap = gctx.res.getBitmap(R.mipmap.note_hold_head)
        lineBitmap = gctx.res.getBitmap(R.mipmap.note_hold_line)
        tailBitmap = gctx.res.getBitmap(R.mipmap.note_hold_tail)
    }

    override val collisionRect: RectF
        get() {
            val halfW = width / 2f
            val halfH = height / 2f

            if (isLongNote) {
                val noteLengthPx = speed * (lengthMs / 1000f)

                val slimHalfH = halfH * 0.3f

                fixedCollisionRect.set(
                    x - halfW,
                    y - slimHalfH,
                    x + noteLengthPx + halfW,
                    y + slimHalfH
                )
            } else {
                val slimHalfH = halfH * 0.6f
                fixedCollisionRect.set(
                    x - halfW,
                    y - slimHalfH,
                    x + halfW,
                    y + slimHalfH
                )
            }
            return fixedCollisionRect
        }

    fun reset(lane: Player.State, speed: Float, lengthMs: Long = 0L) {
        this.lane = lane
        this.speed = speed
        this.lengthMs = lengthMs
        this.isHolding = false

        val targetLaneY = if (lane == Player.State.UP_ATK) 300f else 500f

        if (isLongNote) {
            this.progress = 1f
            p2Y = targetLaneY

            setCenter(1600f, targetLaneY)
        } else {
            this.progress = 0f
            p0X = 1600f; p1X = 1400f; p2X = 1200f
            p2Y = targetLaneY; p1Y = targetLaneY

            if (lane == Player.State.UP_ATK) {
                p0Y = -50f
            } else {
                p0Y = 950f
            }

            interpolationSpeed = speed / (p0X - p2X)
            setCenter(p0X, p0Y)
        }

        val desiredWidth = 140f
        val bitmapWidth = bitmap?.width ?: 100
        val bitmapHeight = bitmap?.height ?: 100
        val aspectRatio = bitmapHeight.toFloat() / bitmapWidth.toFloat()
        val desiredHeight = desiredWidth * aspectRatio

        setSize(desiredWidth, desiredHeight)
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

    override fun draw(canvas: Canvas) {
        if (!isLongNote) {
            super.draw(canvas)
            return
        }

        // --- 롱노트 전용 3단 조립 렌더링 ---
        val currentHeadX = x
        val currentY = y
        val halfW = width / 2f
        val halfH = height / 2f

        val noteLengthPx = speed * (lengthMs / 1000f)

        if (isHolding) {
            alphaPaint.alpha = 120
        } else {
            alphaPaint.alpha = 255
        }

        lineBitmap?.let {
            componentRect.set(
                currentHeadX,
                currentY - halfH,
                currentHeadX + noteLengthPx,
                currentY + halfH
            )
            canvas.drawBitmap(it, null, componentRect, alphaPaint)
        }

        tailBitmap?.let {
            val tailLeft = currentHeadX + noteLengthPx - halfW
            componentRect.set(
                tailLeft,
                currentY - halfH,
                tailLeft + width,
                currentY + halfH
            )
            canvas.drawBitmap(it, null, componentRect, alphaPaint)
        }

        headBitmap?.let {
            componentRect.set(
                currentHeadX - halfW,
                currentY - halfH,
                currentHeadX + halfW,
                currentY + halfH
            )
            canvas.drawBitmap(it, null, componentRect, alphaPaint)
        }
    }

    override fun onRecycle() {
        progress = 0f
        lengthMs = 0L
        isHolding = false
    }
}