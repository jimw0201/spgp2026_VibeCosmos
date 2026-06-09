package kr.ac.tukorea.jmw.vibecosmos.game.manager

import kr.ac.tukorea.jmw.a2dg.scene.World
import kr.ac.tukorea.jmw.a2dg.view.GameContext
import kr.ac.tukorea.jmw.vibecosmos.game.data.SongConfig
import kr.ac.tukorea.jmw.vibecosmos.game.main.MainScene
import kr.ac.tukorea.jmw.vibecosmos.game.main.Note
import kr.ac.tukorea.jmw.vibecosmos.game.main.Player
import java.util.*

class NoteManager(
    private val gctx: GameContext,
    private val world: World<MainScene.Layer>,
    private val config: SongConfig
) {
    data class NoteInfo(val timeMs: Long, val lane: Player.State, val lengthMs: Long = 0L)
    private val noteQueue: Queue<NoteInfo> = LinkedList()

    private var currentTimeMs: Long = 0

    init {
        loadChart()
    }

    private fun loadChart() {
        try {
            gctx.view.context.assets.open(config.chartFileName).bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val parts = line.split("|")
                    
                    if (parts.size >= 2) {
                        val time = parts[0].trim().toLong()
                        val laneIdx = parts[1].trim().toInt()
                        val lane = if (laneIdx == 0) Player.State.UP_ATK else Player.State.DOWN_ATK

                        val lengthMs = if (parts.size == 3) parts[2].trim().toLong() else 0L

                        noteQueue.add(NoteInfo(time, lane, lengthMs))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun update(musicTimeMs: Long, onMiss: () -> Unit) {
        currentTimeMs = musicTimeMs

        val travelTime = (1300f / config.noteSpeed * 1000).toLong()

        while (noteQueue.isNotEmpty() && (noteQueue.peek()!!.timeMs - travelTime) <= currentTimeMs) {
            val info = noteQueue.poll()!!
            spawnNote(info.lane, info.lengthMs)
        }

        // 화면 밖으로 나간 노트 체크
        val notes = world.objectsAt(MainScene.Layer.NOTES)
        var i = 0
        while (i < notes.size) {
            val note = notes[i] as? Note
            if (note != null && note.x < 100f) {
                onMiss()
                world.remove(note, MainScene.Layer.NOTES)
            } else {
                i++
            }
        }
    }

    private fun spawnNote(lane: Player.State, lengthMs: Long) {
        val note = world.obtain(Note::class.java) ?: Note(gctx)
        note.reset(lane, config.noteSpeed, lengthMs)
        world.add(note, MainScene.Layer.NOTES)
    }
}