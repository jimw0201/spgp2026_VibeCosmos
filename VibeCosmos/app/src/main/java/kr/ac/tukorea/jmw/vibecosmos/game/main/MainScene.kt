package kr.ac.tukorea.jmw.vibecosmos.game.main

import android.view.MotionEvent
import kr.ac.tukorea.jmw.a2dg.scene.Scene
import kr.ac.tukorea.jmw.a2dg.view.GameContext
import kr.ac.tukorea.jmw.a2dg.objects.HorzScrollBackground
import kr.ac.tukorea.jmw.a2dg.scene.World
import kr.ac.tukorea.jmw.vibecosmos.R
import kr.ac.tukorea.jmw.vibecosmos.game.data.SongConfig
import kr.ac.tukorea.jmw.vibecosmos.game.manager.NoteManager
import kr.ac.tukorea.jmw.vibecosmos.game.manager.ScoreManager
import kr.ac.tukorea.jmw.vibecosmos.game.manager.SoundManager
import android.media.MediaPlayer

class MainScene(gctx: GameContext, val config: SongConfig) : Scene(gctx) {
    // 그리기 순서를 결정하는 레이어 정의
    enum class Layer {
        BG, PLAYER, NOTES, UI
    }
    override val clipsRect = true

    // 플레이어 객체 생성
    private val player = Player(gctx)

    // 로직 관련 Manager 및 UI 클래스 인스턴스
    private val soundManager = SoundManager(gctx.view.context)
    private val scoreManager = ScoreManager()
    private lateinit var noteManager: NoteManager
    private val hud = Hud()

    // 사운드 플레이어 관련
    private var mediaPlayer: MediaPlayer? = null
    private var readyMediaPlayer: MediaPlayer? = null
    private var isMusicStarted = false
    private var isReadyStarted = false

    private val READY_DURATION = 5000L
    private var sceneStartTime = 0L

    // 판정 기준이 되는 상수
    private val TARGET_X = 400f

    init {
        readyMediaPlayer = MediaPlayer.create(gctx.view.context, R.raw.readygo)

        // config에 저장된 musicResId를 가져와서 재생 시작
        mediaPlayer = MediaPlayer.create(gctx.view.context, config.musicResId).apply {
            isLooping = false
        }
    }

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
        noteManager = NoteManager(gctx, this, config)
    }

    override fun draw(canvas: android.graphics.Canvas) {
        super.draw(canvas)
        hud.draw(canvas, scoreManager, player)
    }

    override fun update(gctx: GameContext) {
        super.update(gctx)

        val now = System.currentTimeMillis()

        // 씬 진입 후 첫 업데이트 시 Ready 사운드 재생 시작
        if (!isReadyStarted) {
            readyMediaPlayer?.start()
            sceneStartTime = now
            isReadyStarted = true
        }

        // 상대 시간 계산
        val relativeTime = (now - sceneStartTime) - READY_DURATION

        // READY_DURATION이 지나서 relativeTime이 0이 되면 메인 음악 시작
        if (!isMusicStarted && relativeTime >= 0) {
            mediaPlayer?.start()
            isMusicStarted = true

            // 사용이 끝난 Ready 사운드 리소스 해제
            readyMediaPlayer?.release()
            readyMediaPlayer = null
        }

        // NoteManager 업데이트
        // 음악 시작 전엔 relativeTime(음수)을, 시작 후엔 실제 음악 포지션을 전달
        val currentMusicPos = if (isMusicStarted) {
            mediaPlayer?.currentPosition?.toLong() ?: relativeTime
        } else {
            relativeTime
        }

        noteManager.update(currentMusicPos) {
            scoreManager.onMiss()
        }

        checkPlayerCollision()
    }

    private fun checkPlayerCollision() {
        // .toMutableList()를 사용하여 현재 시점의 리스트 복사본을 만들기
        val notes = world.objectsAt(Layer.NOTES).toMutableList()
        val it = notes.iterator()

        while (it.hasNext()) {
            val note = it.next() as? Note ?: continue

            // 플레이어의 히트박스와 노트의 히트박스가 겹치는지 확인
            if (android.graphics.RectF.intersects(player.collisionRect, note.collisionRect)) {
                // 충돌 시 로직 실행
                player.hp -= 10

                scoreManager.resetCombo()

                // 원본 world에서 노트를 삭제
                world.remove(note, Layer.NOTES)
            }
        }
    }

    private fun checkHit(attackState: Player.State) {
        val notes = world.objectsAt(Layer.NOTES)
        var closestNote: Note? = null
        var minDistance = Float.MAX_VALUE

        val MAX_HIT_DISTANCE = 100f

        for (obj in notes) {
            val note = obj as? Note ?: continue
            if (note.lane != attackState) continue
            val distance = Math.abs(note.x - TARGET_X)

            // 탐색 범위를 좁혀서, 판정 범위 밖의 노트는 아예 건드리지 않게 함
            if (distance < MAX_HIT_DISTANCE && distance < minDistance) {
                minDistance = distance
                closestNote = note
            }
        }

        if (closestNote != null) {
            val isHit = scoreManager.addScore(minDistance)

            if (isHit) {
                soundManager.playHit()
                world.remove(closestNote, Layer.NOTES)
            }
        }
    }

    // 터치 입력 처리
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN) return super.onTouchEvent(event)

        val currentPos = mediaPlayer?.currentPosition ?: 0
        val laneIdx = if (event.x > gctx.view.width / 2) 1 else 0

        android.util.Log.d("CHART_LOG", "$currentPos | $laneIdx")

        val screenCenter = gctx.view.width / 2

        val attackState = if (event.x > screenCenter) {
            soundManager.playSwingDown()
            player.attackDown()
            Player.State.DOWN_ATK
        } else {
            soundManager.playSwingUp()
            player.attackUp()
            Player.State.UP_ATK
        }

        checkHit(attackState)
        return true
    }
}