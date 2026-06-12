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
import android.util.Log

class MainScene(gctx: GameContext, val config: SongConfig) : Scene(gctx) {
    enum class Layer {
        BG, NOTES, PLAYER, UI
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

    private var isUpLaneHolding = false
    private var isDownLaneHolding = false

    private var upLaneTouchStartMs = 0L
    private var downLaneTouchStartMs = 0L

    init {
        val context = gctx.view.context
        readyMediaPlayer = MediaPlayer.create(context, R.raw.readygo)

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
        checkLongNoteHold(elapsedSeconds)
    }

    private fun checkPlayerCollision() {
        val notes = world.objectsAt(Layer.NOTES).toMutableList()
        val it = notes.iterator()

        while (it.hasNext()) {
            val note = it.next() as? Note ?: continue

            if (android.graphics.RectF.intersects(player.collisionRect, note.collisionRect)) {

                if (note.isLongNote) {
                    if (!note.isHolding) {
                        soundManager.playDamage()
                        player.hp -= 10
                        scoreManager.resetCombo()

                        world.remove(note, Layer.NOTES)
                    }
                } else {
                    soundManager.playDamage()
                    player.hp -= 10
                    scoreManager.resetCombo()
                    world.remove(note, Layer.NOTES)
                }
            }
        }
    }

    private fun checkLongNoteHold(elapsedSeconds: Float) {
        val notes = world.objectsAt(Layer.NOTES).toMutableList()
        var anyNoteHolding = false

        for (obj in notes) {
            val note = obj as? Note ?: continue
            if (!note.isLongNote) continue

            val noteLengthPx = note.speed * (note.lengthMs / 1000f)
            val noteHeadX = note.x
            val noteTailX = noteHeadX + noteLengthPx

            if (TARGET_X in noteHeadX..noteTailX) {

                val isTouchMovingOrHolding =
                    if (note.lane == Player.State.UP_ATK) isUpLaneHolding else isDownLaneHolding

                if (isTouchMovingOrHolding && note.isHitValidated) {
                    scoreManager.addHoldScore(elapsedSeconds)
                    anyNoteHolding = true
                    note.isHolding = true

                    if (player.state != Player.State.HOLD_ATK) {
                        player.state = Player.State.HOLD_ATK
                    }

                    val targetY = if (note.lane == Player.State.UP_ATK) 300f else 500f
                    player.setCenter(player.x, targetY)

                } else {
                    if (note.isHolding) {
                        note.isHolding = false
                        scoreManager.onMiss()
                        world.remove(note, Layer.NOTES)
                        continue
                    }
                }
            }
            else {
                if (noteTailX < TARGET_X && note.isHolding) {
                    note.isHolding = false
                    world.remove(note, Layer.NOTES)
                    continue
                }
            }
        }

        if (!anyNoteHolding && player.state == Player.State.HOLD_ATK) {
            player.state = Player.State.RUN
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
            if (closestNote.isLongNote) {
                closestNote.isHitValidated = true

                scoreManager.addScore(minDistance)
                closestNote.isHolding = true
                soundManager.playHit()
            } else {
                val isHit = scoreManager.addScore(minDistance)
                if (isHit) {
                    soundManager.playHit()
                    world.remove(closestNote, Layer.NOTES)
                }
            }
        }
    }


    // 채보 로그를 보려면 Logcat에서 CHART_MAKER 검색
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val screenCenter = gctx.view.width / 2
        val isRightSide = event.x > screenCenter

        val now = System.currentTimeMillis()
        val currentMusicTime = if (isMusicStarted) {
            mediaPlayer?.currentPosition?.toLong() ?: (now - sceneStartTime - READY_DURATION)
        } else {
            now - sceneStartTime - READY_DURATION
        }

        val isRecordable = currentMusicTime >= 0

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                if (isRightSide) {
                    isDownLaneHolding = true
                    soundManager.playSwingDown()

                    if (player.state != Player.State.HOLD_ATK) {
                        player.attackDown(forceReset = true)
                    }

                    checkHit(Player.State.DOWN_ATK)
                    if (isRecordable) downLaneTouchStartMs = currentMusicTime
                } else {
                    isUpLaneHolding = true
                    soundManager.playSwingUp()

                    if (player.state != Player.State.HOLD_ATK) {
                        player.attackUp(forceReset = true)
                    }

                    checkHit(Player.State.UP_ATK)
                    if (isRecordable) upLaneTouchStartMs = currentMusicTime
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (isRightSide) {
                    isDownLaneHolding = true
                    isUpLaneHolding = false
                } else {
                    isUpLaneHolding = true
                    isDownLaneHolding = false
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                if (isRightSide) {
                    isDownLaneHolding = false
                    if (isRecordable && downLaneTouchStartMs > 0) {
                        val duration = currentMusicTime - downLaneTouchStartMs

                        if (duration < 300) {
                            Log.d("CHART_MAKER", "$downLaneTouchStartMs | 1")
                        } else {
                            Log.d("CHART_MAKER", "$downLaneTouchStartMs | 1 | $duration")
                        }
                        downLaneTouchStartMs = 0L
                    }
                } else {
                    isUpLaneHolding = false

                    if (isRecordable && upLaneTouchStartMs > 0) {
                        val duration = currentMusicTime - upLaneTouchStartMs

                        if (duration < 300) {
                            Log.d("CHART_MAKER", "$upLaneTouchStartMs | 0")
                        } else {
                            Log.d("CHART_MAKER", "$upLaneTouchStartMs | 0 | $duration")
                        }
                        upLaneTouchStartMs = 0L
                    }
                }

                if (!isUpLaneHolding && !isDownLaneHolding) {
                    if (player.state == Player.State.HOLD_ATK) {
                        player.state = Player.State.RUN
                    }
                }
            }
        }
        return true
    }
}