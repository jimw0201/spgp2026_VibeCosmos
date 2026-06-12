package kr.ac.tukorea.jmw.vibecosmos.game.main

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kr.ac.tukorea.jmw.vibecosmos.game.manager.ScoreManager

class Hud {
    // --- UI 그리기용 Paint 설정 ---
    private val scorePaint = Paint().apply {
        color = Color.WHITE
        textSize = 60f
        isFakeBoldText = true
        textAlign = Paint.Align.LEFT
    }

    private val hpPaint = Paint().apply {
        color = Color.RED
        textSize = 60f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }

    private val upperMarkerPaint = Paint().apply {
        color = Color.CYAN
        style = Paint.Style.STROKE
        strokeWidth = 8f
        alpha = 180
    }

    private val lowerMarkerPaint = Paint().apply {
        color = Color.MAGENTA
        style = Paint.Style.STROKE
        strokeWidth = 8f
        alpha = 180
    }

    private val judgmentPaint = Paint().apply {
        textSize = 90f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }

    private val comboPaint = Paint().apply {
        color = Color.YELLOW
        textSize = 80f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }

    // 그리기 기준 좌표
    private val TARGET_X = 400f
    private val UPPER_LANE_Y = 300f
    private val LOWER_LANE_Y = 500f

    // 모든 UI 요소를 그리는 함수
    fun draw(canvas: Canvas, scoreManager: ScoreManager, player: Player) {
        // 점수 및 HP 표시
        canvas.drawText("Score: ${scoreManager.score}", 50f, 80f, scorePaint)
        canvas.drawText("HP: ${player.hp}", 800f, 850f, hpPaint)

        // 타격 지점 가이드 원
        canvas.drawCircle(TARGET_X, UPPER_LANE_Y, 40f, upperMarkerPaint)
        canvas.drawCircle(TARGET_X, LOWER_LANE_Y, 40f, lowerMarkerPaint)

        // 콤보 표시
        if (scoreManager.combo > 0) {
            canvas.drawText("${scoreManager.combo} COMBO", 800f, 180f, comboPaint)
        }

        val judgment = scoreManager.lastJudgment

        if (judgment.isNotEmpty() && judgment != "HOLD") {

            when (judgment) {
                "PERFECT" -> judgmentPaint.color = Color.GREEN
                "GREAT"   -> judgmentPaint.color = Color.YELLOW
                else      -> judgmentPaint.color = Color.RED
            }

            val progress = Math.min(scoreManager.judgmentDisplayTime / scoreManager.JUDGMENT_DURATION, 1.0f)

            val alpha = ((1.0f - progress) * 255).toInt()
            judgmentPaint.alpha = alpha

            val baseEventY = if (player.state == Player.State.UP_ATK || (player.state == Player.State.HOLD_ATK && player.y < 400f)) 250f else 450f
            val animatedY = baseEventY - (progress * 80f)

            canvas.drawText(judgment, 650f, animatedY, judgmentPaint)
        }
    }
}