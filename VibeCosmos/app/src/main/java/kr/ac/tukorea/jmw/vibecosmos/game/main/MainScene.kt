package kr.ac.tukorea.jmw.vibecosmos.game.main

import android.media.AudioAttributes
import android.media.SoundPool
import android.view.MotionEvent
import kr.ac.tukorea.jmw.a2dg.scene.Scene
import kr.ac.tukorea.jmw.a2dg.view.GameContext
import kr.ac.tukorea.jmw.a2dg.objects.HorzScrollBackground
import kr.ac.tukorea.jmw.a2dg.scene.World
import kr.ac.tukorea.jmw.vibecosmos.R

class MainScene(gctx: GameContext) : Scene(gctx) {
    // 그리기 순서를 결정하는 레이어 정의
    enum class Layer {
        BG, PLAYER, NOTES, UI
    }
    override val clipsRect = true

    // 플레이어 객체 생성
    private val player = Player(gctx)
    // 노트 생성 주기 관리 타이머
    private var spawnTimer = 0f
    // 화면에 표시할 판정 텍스트
    private var lastJudgment = ""
    // 판정 텍스트 노출 시간 관리
    private var judgmentTimer = 0f

    // 현재 점수
    var score = 0
    // 현재 연속 콤보 수
    var combo = 0

    // SoundPool 및 사운드 ID 변수 선언
    private val soundPool: SoundPool
    private var hitSoundId: Int = 0

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        hitSoundId = soundPool.load(gctx.view.context, R.raw.hitsound_000, 1)
    }

    // --- UI 및 게임 요소 그리기용 Paint 설정 ---
    // 스코어
    private val scorePaint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 60f
        isFakeBoldText = true
        textAlign = android.graphics.Paint.Align.LEFT
    }

    // 체력
    private val hpPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.RED
        textSize = 60f
        isFakeBoldText = true
        textAlign = android.graphics.Paint.Align.CENTER
    }

    // 상단 레인 타격 타이밍 마커
    private val upperMarkerPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.CYAN
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 8f
        alpha = 180
    }

    // 하단 레인 타격 타이밍 마커
    private val lowerMarkerPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.MAGENTA
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 8f
        alpha = 180
    }

    // 판정 메시지
    private val judgmentPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.YELLOW
        textSize = 100f
        isFakeBoldText = true
        textAlign = android.graphics.Paint.Align.CENTER
    }

    // 콤보
    private val comboPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.YELLOW
        textSize = 80f
        isFakeBoldText = true
        textAlign = android.graphics.Paint.Align.CENTER
    }

    // 판정 기준이 되는 X좌표와 각 레인의 Y좌표 상수
    private val TARGET_X = 400f
    private val UPPER_LANE_Y = 300f
    private val LOWER_LANE_Y = 500f

    // 게임 월드 초기화: 배경 레이어와 플레이어 추가
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

        // 점수 및 HP 표시
        canvas.drawText("Score: $score", 50f, 80f, scorePaint)
        canvas.drawText("HP: ${player.hp}", 800f, 850f, hpPaint)

        // 타격 지점을 시각적으로 보여주는 가이드 원 표시
        canvas.drawCircle(TARGET_X, UPPER_LANE_Y, 40f, upperMarkerPaint)
        canvas.drawCircle(TARGET_X, LOWER_LANE_Y, 40f, lowerMarkerPaint)

        // 콤보가 있을 대만 화면에 표시
        if (combo > 0) {
            canvas.drawText("${combo} COMBO", 800f, 550f, comboPaint)
        }

        // 최신 판정 결과를 잠시 동안 화면에 표시
        if (lastJudgment.isNotEmpty()) {
            canvas.drawText(lastJudgment, 800f, 450f, judgmentPaint)
        }
    }

    override fun update(gctx: GameContext) {
        super.update(gctx)

        val elapsedSeconds = gctx.frameTime
        spawnTimer += elapsedSeconds

        // 판정 텍스트가 0.8초 동안만 보이도록
        if (lastJudgment.isNotEmpty()) {
            judgmentTimer += elapsedSeconds
            if (judgmentTimer > 0.8f) {
                lastJudgment = ""
                judgmentTimer = 0f
            }
        }

        // 1.5초마다 새로운 노트 생성
        if (spawnTimer > 1.5f) {
            spawnNoteWithPooling()
            spawnTimer = 0f
        }

        // 화면 왼쪽 밖으로 벗어난 노트를 체크하여 MISS 처리
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

    // 플레이어의 공격 상태와 노트의 거리를 계산하여 판정 수행
    private fun checkHit(attackState: Player.State) {
        val notes = world.objectsAt(Layer.NOTES)
        var closestNote: Note? = null
        var minDistance = Float.MAX_VALUE

        // 현재 공격한 레인에 있는 가장 가까운 노트를 찾음
        for (obj in notes) {
            val note = obj as? Note ?: continue
            if (note.lane != attackState) continue
            val distance = Math.abs(note.x - TARGET_X)
            if (distance < 200f && distance < minDistance) {
                minDistance = distance
                closestNote = note
            }
        }

        // 유효 거리 내에 노트가 있다면 점수 계산
        if (closestNote != null) {
            judgmentTimer = 0f

            val baseScore = when {
                minDistance < 40f -> {
                    lastJudgment = "PERFECT"
                    combo++
                    playHitSound()
                    100
                }
                minDistance < 100f -> {
                    lastJudgment = "GREAT"
                    combo++
                    playHitSound()
                    50
                }
                else -> {
                    lastJudgment = "MISS"
                    combo = 0
                    0
                }
            }

            // 콤보가 높을수록 보너스 점수 붑여
            val multiplier = 1.0f + (Math.min(combo / 10, 10) * 0.1f)
            score += (baseScore * multiplier).toInt()

            world.remove(closestNote, Layer.NOTES)
        }
    }

    // 사운드 재생 함수
    private fun playHitSound() {
        if (hitSoundId != 0) {
            // 사운드ID, 왼쪽 볼륨, 오른쪽 볼륨, 우선순위, 반복여부, 재생속도
            soundPool.play(hitSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }

    // 객체 풀링을 사용하여 노트를 생성하고 랜덤 레인에 배치
    private fun spawnNoteWithPooling() {
        val randomLane = if (Math.random() > 0.5) Player.State.UP_ATK else Player.State.DOWN_ATK
        val note = world.obtain(Note::class.java) ?: Note(gctx)

        note.reset(randomLane)

        world.add(note, Layer.NOTES)
    }

    // 터치 입력 처리
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