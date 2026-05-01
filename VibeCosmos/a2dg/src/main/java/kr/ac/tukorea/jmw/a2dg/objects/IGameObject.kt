package kr.ac.tukorea.jmw.a2dg.objects

import android.graphics.Canvas
import kr.ac.tukorea.jmw.a2dg.view.GameContext

interface IGameObject {
    fun update(gctx: GameContext)
    fun draw(canvas: Canvas)
}
