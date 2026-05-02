package kr.ac.tukorea.jmw.vibecosmos.game.main

import android.view.MotionEvent
import kr.ac.tukorea.jmw.a2dg.scene.Scene
import kr.ac.tukorea.jmw.a2dg.view.GameContext
import kr.ac.tukorea.jmw.a2dg.objects.HorzScrollBackground
import kr.ac.tukorea.jmw.a2dg.scene.World
import kr.ac.tukorea.jmw.vibecosmos.R


class MainScene(gctx: GameContext) : Scene(gctx) {
    enum class Layer {
        BG, PLAYER
    }
    override val clipsRect = true

    val player = Player(gctx)

    override val world = World(Layer.entries.toTypedArray()).apply {
        listOf(
            // R.mipmap.stage_bg to -150f,
            R.mipmap.stage_bg1 to -150f,
            R.mipmap.stage_bg2 to -150f,
        ).forEach { (resId, speed) ->
            add(HorzScrollBackground(gctx, resId, speed), Layer.BG)
        }
        add(player, Layer.PLAYER)
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