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
    enum class Layer {
        BG, PLAYER, NOTES, UI
    }
    override val clipsRect = true

    private val player = Player(gctx)
    private val soundManager = SoundManager(gctx.view.context)
    private val scoreManager = ScoreManager()
    private lateinit var noteManager: NoteManager
    private val hud = Hud()

    private var mediaPlayer: MediaPlayer? = null
    private var readyMediaPlayer: MediaPlayer? = null
    private var isMusicStarted = false
    private var isReadyStarted = false

    private val READY_DURATION = 5000L
    private var sceneStartTime = 0L
    private val TARGET_X = 400f

    init {
        val context = gctx.view.context
        readyMediaPlayer = MediaPlayer.create(context, R.raw.readygo)

        // config의 String 정보를 활용해 dynamic하게 리소스 ID를 가져와 세팅
        val musicResId = config.getMusicResId(context)
        mediaPlayer = MediaPlayer.create(context, musicResId).apply {
            isLooping = false
        }
    }

    override val world = World(Layer.entries.toTypedArray()).apply {
        val context = gctx.view.context
        val bgResId = config.getBgResId(context)

        listOf(
            bgResId to -150f,
            R.mipmap.stage_bg3 to -200f,
            R.mipmap.stage_bg2 to -150f,
        ).forEach { (resId, speed) ->
            if (resId != 0) { // 리소스 검색 실패 방어를 위한 분기
                add(HorzScrollBackground(gctx, resId, speed), Layer.BG)
            }
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
        val elapsedSeconds = gctx.frameTime
        scoreManager.update(elapsedSeconds)

        val now = System.currentTimeMillis()
        if (!isReadyStarted) {
            readyMediaPlayer?.start()
            sceneStartTime = now
            isReadyStarted = true
        }

        val relativeTime = (now - sceneStartTime) - READY_DURATION

        if (!isMusicStarted && relativeTime >= 0) {
            mediaPlayer?.start()
            isMusicStarted = true
            readyMediaPlayer?.release()
            readyMediaPlayer = null
        }

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
        val notes = world.objectsAt(Layer.NOTES).toMutableList()
        val it = notes.iterator()

        while (it.hasNext()) {
            val note = it.next() as? Note ?: continue
            if (android.graphics.RectF.intersects(player.collisionRect, note.collisionRect)) {
                soundManager.playDamage()
                player.hp -= 10
                scoreManager.resetCombo()
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

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN) return super.onTouchEvent(event)

        val laneIdx = if (event.x > gctx.view.width / 2) 1 else 0
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