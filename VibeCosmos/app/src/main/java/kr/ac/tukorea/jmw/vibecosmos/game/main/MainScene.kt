package kr.ac.tukorea.jmw.vibecosmos.game.main

import android.view.MotionEvent
import kr.ac.tukorea.jmw.a2dg.scene.Scene
import kr.ac.tukorea.jmw.a2dg.view.GameContext
import kr.ac.tukorea.jmw.a2dg.objects.HorzScrollBackground
import kr.ac.tukorea.jmw.a2dg.objects.Sprite
import kr.ac.tukorea.jmw.a2dg.scene.World
import kr.ac.tukorea.jmw.vibecosmos.R


class MainScene(gctx: GameContext) : Scene(gctx) {
    enum class Layer {
        BG, PLAYER, NOTES
    }
    override val clipsRect = true

    private val player = Player(gctx)
    private var spawnTimer = 0f

    override val world = World(Layer.entries.toTypedArray()).apply {
        listOf(
            // R.mipmap.stage_bg to -150f,
            R.mipmap.stage_bg1 to -150f,
            R.mipmap.stage_bg3 to -200f,
            R.mipmap.stage_bg2 to -150f,
        ).forEach { (resId, speed) ->
            add(HorzScrollBackground(gctx, resId, speed), Layer.BG)
        }
        add(player, Layer.PLAYER)
    }

    override fun update(gctx: GameContext) {
        super.update(gctx)

        val elapsedSeconds = gctx.frameTime
        spawnTimer += elapsedSeconds
        if (spawnTimer > 1.5f) {
            spawnNote()
            spawnTimer = 0f
        }
        
        val notes = world.objectsAt(Layer.NOTES)

        var i = 0
        while (i < notes.size) {
            val note = notes[i] as? Sprite

            if (note != null && note.x < -100f) {
                world.remove(note, Layer.NOTES)
            } else {
                i++
            }
        }
    }

    private fun spawnNote() {
        val randomLane = if (Math.random() > 0.5) Player.State.UP_ATK else Player.State.DOWN_ATK

        world.add(Note(gctx, randomLane), Layer.NOTES)
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        val screenCenter = gctx.view.width / 2
        if (event.x > screenCenter) {
            if (event.action == MotionEvent.ACTION_DOWN) {
                player.attackDown()
                return true
            }
        } else {
            if (event.action == MotionEvent.ACTION_DOWN) {
                player.attackUp()
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}