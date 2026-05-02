package kr.ac.tukorea.jmw.vibecosmos.game.main

import android.view.MotionEvent
import kr.ac.tukorea.jmw.a2dg.scene.Scene
import kr.ac.tukorea.jmw.a2dg.view.GameContext
import kr.ac.tukorea.jmw.a2dg.objects.HorzScrollBackground
import kr.ac.tukorea.jmw.a2dg.scene.World
import kr.ac.tukorea.jmw.vibecosmos.R

class MainScene(gctx: GameContext) : Scene(gctx) {
    enum class Layer {
        BG, PLAYER, NOTES, UI
    }
    override val clipsRect = true

    private val player = Player(gctx)
    private var spawnTimer = 0f

    private var lastJudgment = ""
    private var judgmentTimer = 0f

    var score = 0

    var combo = 0

    private val scorePaint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 60f
        isFakeBoldText = true
        textAlign = android.graphics.Paint.Align.LEFT
    }

    private val hpPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.RED
        textSize = 60f
        isFakeBoldText = true
        textAlign = android.graphics.Paint.Align.CENTER
    }

    private val upperMarkerPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.CYAN
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 8f
        alpha = 180
    }

    private val lowerMarkerPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.MAGENTA
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 8f
        alpha = 180
    }

    private val judgmentPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.YELLOW
        textSize = 100f
        isFakeBoldText = true
        textAlign = android.graphics.Paint.Align.CENTER
    }

    private val comboPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.YELLOW
        textSize = 80f
        isFakeBoldText = true
        textAlign = android.graphics.Paint.Align.CENTER
    }

    private val TARGET_X = 400f
    private val UPPER_LANE_Y = 300f
    private val LOWER_LANE_Y = 500f

    override val world = World(Layer.entries.toTypedArray()).apply {
        listOf(
            R.mipmap.stage_bg1 to -150f,
            R.mipmap.stage_bg3 to -200f,
            R.mipmap.stage_bg2 to -150f,
        ).forEach { (resId, speed) ->
            add(HorzScrollBackground(gctx, resId, speed), Layer.BG)
        }
        add(player, Layer.PLAYER)
    }

    override fun draw(canvas: android.graphics.Canvas) {
        super.draw(canvas)

        canvas.drawText("Score: $score", 50f, 80f, scorePaint)
        canvas.drawText("HP: ${player.hp}", 800f, 850f, hpPaint)

        canvas.drawCircle(TARGET_X, UPPER_LANE_Y, 40f, upperMarkerPaint)
        canvas.drawCircle(TARGET_X, LOWER_LANE_Y, 40f, lowerMarkerPaint)

        if (combo > 0) {
            canvas.drawText("${combo} COMBO", 800f, 550f, comboPaint)
        }

        if (lastJudgment.isNotEmpty()) {
            canvas.drawText(lastJudgment, 800f, 450f, judgmentPaint)
        }
    }

    override fun update(gctx: GameContext) {
        super.update(gctx)

        val elapsedSeconds = gctx.frameTime
        spawnTimer += elapsedSeconds

        if (lastJudgment.isNotEmpty()) {
            judgmentTimer += elapsedSeconds
            if (judgmentTimer > 0.8f) {
                lastJudgment = ""
                judgmentTimer = 0f
            }
        }

        if (spawnTimer > 1.5f) {
            spawnNoteWithPooling()
            spawnTimer = 0f
        }

        val notes = world.objectsAt(Layer.NOTES)
        var i = 0
        while (i < notes.size) {
            val note = notes[i] as? Note
            if (note != null && note.x < -100f) {
                lastJudgment = "MISS"
                judgmentTimer = 0f

                combo = 0
                player.hp -= 10

                world.remove(note, Layer.NOTES)
            } else {
                i++
            }
        }
    }

    private fun checkHit(attackState: Player.State) {
        val notes = world.objectsAt(Layer.NOTES)
        var closestNote: Note? = null
        var minDistance = Float.MAX_VALUE

        for (obj in notes) {
            val note = obj as? Note ?: continue
            if (note.lane != attackState) continue
            val distance = Math.abs(note.x - TARGET_X)
            if (distance < 200f && distance < minDistance) {
                minDistance = distance
                closestNote = note
            }
        }

        if (closestNote != null) {
            judgmentTimer = 0f

            val baseScore = when {
                minDistance < 40f -> {
                    lastJudgment = "PERFECT"
                    combo++
                    100
                }
                minDistance < 100f -> {
                    lastJudgment = "GREAT"
                    combo++
                    50
                }
                else -> {
                    lastJudgment = "MISS"
                    combo = 0
                    0
                }
            }
            
            val multiplier = 1.0f + (Math.min(combo / 10, 10) * 0.1f)
            score += (baseScore * multiplier).toInt()

            world.remove(closestNote, Layer.NOTES)
        }
    }

    private fun spawnNoteWithPooling() {
        val randomLane = if (Math.random() > 0.5) Player.State.UP_ATK else Player.State.DOWN_ATK
        val note = world.obtain(Note::class.java) ?: Note(gctx)

        note.reset(randomLane)

        world.add(note, Layer.NOTES)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN) return super.onTouchEvent(event)

        val screenCenter = gctx.view.width / 2
        val attackState = if (event.x > screenCenter) {
            player.attackDown()
            Player.State.DOWN_ATK
        } else {
            player.attackUp()
            Player.State.UP_ATK
        }

        checkHit(attackState)
        return true
    }
}