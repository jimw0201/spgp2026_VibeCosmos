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
    // 채보 데이터를 담을 클래스와 큐 생성
    data class NoteInfo(val timeMs: Long, val lane: Player.State)
    private val noteQueue: Queue<NoteInfo> = LinkedList()

    // 게임 시작 후 경과 시간
    private var currentTimeMs: Long = 0

    init {
        loadChart()
    }

    // assets 폴더에서 txt 파일을 읽어 큐에 저장
    private fun loadChart() {
        try {
            gctx.view.context.assets.open(config.chartFileName).bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val parts = line.split("|")
                    if (parts.size == 2) {
                        val time = parts[0].trim().toLong()
                        val laneIdx = parts[1].trim().toInt()
                        val lane = if (laneIdx == 0) Player.State.UP_ATK else Player.State.DOWN_ATK
                        noteQueue.add(NoteInfo(time, lane))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun update(elapsedSeconds: Float, onMiss: () -> Unit) {
        // 시간 업데이트
        currentTimeMs += (elapsedSeconds * 1000).toLong()

        // 현재 시간에 맞는 노트를 큐에서 꺼내 생성
        while (noteQueue.isNotEmpty() && noteQueue.peek()!!.timeMs <= currentTimeMs) {
            val info = noteQueue.poll()!!
            spawnNote(info.lane)
        }

        // 화면 밖으로 나간 노트 체크
        val notes = world.objectsAt(MainScene.Layer.NOTES)
        var i = 0
        while (i < notes.size) {
            val note = notes[i] as? Note
            if (note != null && note.x < -100f) {
                onMiss()
                world.remove(note, MainScene.Layer.NOTES)
            } else {
                i++
            }
        }
    }

    private fun spawnNote(lane: Player.State) {
        val note = world.obtain(Note::class.java) ?: Note(gctx)
        note.reset(lane, config.noteSpeed)
        world.add(note, MainScene.Layer.NOTES)
    }
}