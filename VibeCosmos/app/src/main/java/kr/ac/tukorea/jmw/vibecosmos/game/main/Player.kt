package kr.ac.tukorea.jmw.vibecosmos.game.main

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import kr.ac.tukorea.jmw.a2dg.objects.AnimSprite
import kr.ac.tukorea.jmw.a2dg.objects.IBoxCollidable
import kr.ac.tukorea.jmw.a2dg.view.GameContext
import kr.ac.tukorea.jmw.vibecosmos.R

class Player(gctx: GameContext): AnimSprite(
    gctx,
    R.mipmap.player_run, // 기본 상태
    60f, // 기본 애니메이션 FPS
    frameCount = 32, // 전체 프레임 수
    columns = 8 // 스프라이트 시트의 가로 열 수
    ), IBoxCollidable {

    private val fixedCollisionRect = RectF()

    override val collisionRect: RectF
        get() {
            fixedCollisionRect.set(
                x - WIDTH / 2,
                y - HEIGHT / 2,
                x + WIDTH / 2,
                y + HEIGHT / 2
            )
            return fixedCollisionRect
        }

    enum class State {
        RUN, UP_ATK, DOWN_ATK, HOLD_ATK
    }

    var hp = 200
    private var stateStartTime = System.currentTimeMillis()
    private var currentColumns = 8

    private var holdBitmap: android.graphics.Bitmap? = null
    private val holdFrameCount = 48
    private val holdFps = 60f
    private val holdColumns = 8

    private var holdFrameWidth = 0
    private var holdFrameHeight = 0
    private val holdSrcRect = Rect()

    init {
        this.width = WIDTH
        this.height = HEIGHT
        setCenter(200f, 500f)

        holdBitmap = gctx.res.getBitmap(R.mipmap.player_hold_atk)?.apply {
            holdFrameWidth = this.width / holdColumns
            holdFrameHeight = this.height / 6
        }
    }

    var state = State.RUN
        set(value) {
            if (field == value && value != State.HOLD_ATK) return

            field = value

            if (value == State.HOLD_ATK) {
                this.width = WIDTH * 1.3f
                this.height = HEIGHT * 1.3f
                this.stateStartTime = System.currentTimeMillis()
                return
            }

            val config = when (value) {
                State.RUN -> Quad(R.mipmap.player_run, 32, 1.0f, 60f, 500f)
                State.UP_ATK -> Quad(R.mipmap.player_up_atk, 30, 1.3f, 90f, 300f)
                State.DOWN_ATK -> Quad(R.mipmap.player_down_atk, 22, 1.3f, 90f, 500f)
            }

            bitmap = gctx.res.getBitmap(config.resId)
            this.frameCount = config.frameCount
            this.fps = config.fps
            this.currentColumns = 8

            this.width = WIDTH * config.scale
            this.height = HEIGHT * config.scale
            setCenter(200f, config.targetY)

            this.stateStartTime = System.currentTimeMillis()
        }

    override fun draw(canvas: Canvas) {
        syncDstRect()

        val elapsedSeconds = (System.currentTimeMillis() - stateStartTime) / 1000f

        if (state == State.HOLD_ATK) {
            holdBitmap?.let { bmp ->
                val frameIndex = ((elapsedSeconds * holdFps).toInt()) % holdFrameCount

                val col = frameIndex % holdColumns
                val row = frameIndex / holdColumns

                holdSrcRect.set(
                    col * holdFrameWidth,
                    row * holdFrameHeight,
                    (col + 1) * holdFrameWidth,
                    (row + 1) * holdFrameHeight
                )

                canvas.drawBitmap(bmp, holdSrcRect, dstRect, null)
            }
            return
        }

        val totalDuration = frameCount / fps
        if (state != State.RUN && elapsedSeconds >= totalDuration) {
            state = State.RUN
        }

        val frameIndex = ((elapsedSeconds * fps).toInt()) % frameCount
        val col = frameIndex % currentColumns
        val row = frameIndex / currentColumns

        srcRect?.set(
            col * frameWidth,
            row * frameHeight,
            (col + 1) * frameWidth,
            (row + 1) * frameHeight
        )
        canvas.drawBitmap(bitmap, srcRect, dstRect, null)
    }

    fun attackUp(forceReset: Boolean = false) {
        if (forceReset) {
            state = State.RUN
        }
        state = State.UP_ATK
    }
    fun attackDown(forceReset: Boolean = false) {
        if (forceReset) {
            state = State.RUN
        }
        state = State.DOWN_ATK
    }

    private data class Quad(val resId: Int, val frameCount: Int, val scale: Float, val fps: Float, val targetY: Float)

    companion object {
        const val WIDTH = 200f
        const val HEIGHT = 261f
    }
}