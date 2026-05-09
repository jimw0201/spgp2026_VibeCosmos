package kr.ac.tukorea.jmw.vibecosmos.game.manager

class ScoreManager {
    // 점수 및 콤보 관련 데이터
    var score: Int = 0
        private set
    var combo: Int = 0
        private set
    var lastJudgment: String = ""
        private set

    private var judgmentDisplayTime = 0f

    val JUDGMENT_DURATION = 0.5f

    // 판정 메시지 노출 시간 업데이트
    fun update(elapsedSeconds: Float) {
        if (lastJudgment.isNotEmpty()) {
            judgmentDisplayTime += elapsedSeconds

            if (judgmentDisplayTime >= JUDGMENT_DURATION) {
                lastJudgment = ""
                judgmentDisplayTime = 0f
            }
        }
    }

    // 타격 거리에 따른 점수 산정 로직
    fun addScore(minDistance: Float): Boolean {
        judgmentDisplayTime = 0f

        val baseScore = when {
            minDistance < 50f -> {
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
                onMiss()
                return false
            }
        }

        // 콤보 보너스 계산
        val multiplier = 1.0f + (Math.min(combo / 10, 10) * 0.1f)
        score += (baseScore * multiplier).toInt()
        return true
    }

    // 미스 발생 시 처리
    fun onMiss() {
        lastJudgment = "MISS"
        judgmentDisplayTime = 0f
        combo = 0
    }

    fun resetCombo() {
        combo = 0
    }
}