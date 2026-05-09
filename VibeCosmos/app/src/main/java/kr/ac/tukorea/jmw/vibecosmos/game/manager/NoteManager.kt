package kr.ac.tukorea.jmw.vibecosmos.game.manager

import kr.ac.tukorea.jmw.a2dg.scene.World
import kr.ac.tukorea.jmw.a2dg.view.GameContext
import kr.ac.tukorea.jmw.vibecosmos.game.main.MainScene
import kr.ac.tukorea.jmw.vibecosmos.game.main.Note
import kr.ac.tukorea.jmw.vibecosmos.game.main.Player

class NoteManager(
    private val gctx: GameContext,
    private val world: World<MainScene.Layer>
) {
    private var spawnTimer = 0f

    // 노트 생성 간격
    private val SPAWN_INTERVAL = 1.5f

    fun update(elapsedSeconds: Float, onMiss: () -> Unit) {
        // 노트 생성 타이머 관리
        spawnTimer += elapsedSeconds
        if (spawnTimer > SPAWN_INTERVAL) {
            spawnNoteWithPooling()
            spawnTimer = 0f
        }

        // 화면 밖으로 나간 노트 체크
        val notes = world.objectsAt(MainScene.Layer.NOTES)
        var i = 0
        while (i < notes.size) {
            val note = notes[i] as? Note

            if (note != null && note.x < -100f) {
                onMiss() // 외부(MainScene)에 MISS 발생을 알림
                world.remove(note, MainScene.Layer.NOTES) // 월드에서 제거하여 풀로 반환
            } else {
                i++
            }
        }
    }

    private fun spawnNoteWithPooling() {
        // 랜덤하게 상단/하단 레인 결정
        val randomLane = if (Math.random() > 0.5) Player.State.UP_ATK else Player.State.DOWN_ATK

        // 풀에서 노트를 가져오거나 새로 생성
        val note = world.obtain(Note::class.java) ?: Note(gctx)

        // 노트 상태 초기화 및 월드 추가
        note.reset(randomLane)
        world.add(note, MainScene.Layer.NOTES)
    }
}