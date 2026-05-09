package kr.ac.tukorea.jmw.vibecosmos.game.main

import android.media.AudioAttributes
import android.media.SoundPool
import android.view.MotionEvent
import kr.ac.tukorea.jmw.a2dg.scene.Scene
import kr.ac.tukorea.jmw.a2dg.view.GameContext
import kr.ac.tukorea.jmw.a2dg.objects.HorzScrollBackground
import kr.ac.tukorea.jmw.a2dg.scene.World
import kr.ac.tukorea.jmw.vibecosmos.R
import kr.ac.tukorea.jmw.vibecosmos.game.manager.ScoreManager
import kr.ac.tukorea.jmw.vibecosmos.game.manager.SoundManager

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

    // 로직 관련 Manager 및 UI 클래스 인스턴스
    private val soundManager = SoundManager(gctx.view.context)
    private val scoreManager = ScoreManager()
    private val hud = Hud()

    // 판정 기준이 되는 상수
    private val TARGET_X = 400f

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
        hud.draw(canvas, scoreManager, player)
    }

    override fun update(gctx: GameContext) {
        super.update(gctx)

        val elapsedSeconds = gctx.frameTime
        scoreManager.update(elapsedSeconds)

        spawnTimer += elapsedSeconds

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
                scoreManager.onMiss()
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
            // 판정 및 점수 추가 위임
            val isHit = scoreManager.addScore(minDistance)

            if (isHit) {
                soundManager.playHit()
            }
            world.remove(closestNote, Layer.NOTES)
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