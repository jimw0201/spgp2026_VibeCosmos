package kr.ac.tukorea.jmw.vibecosmos.game.main

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.RectF
import android.view.SurfaceHolder
import android.view.SurfaceView
import kr.ac.tukorea.jmw.vibecosmos.R
import java.lang.Exception

class TitleCharacterView(context: Context) : SurfaceView(context), SurfaceHolder.Callback, Runnable {

    private var renderThread: Thread? = null
    @Volatile private var isRunning = false

    private val srcRect = Rect()
    private val dstRect = RectF()
    
    private var titleBitmap: Bitmap? = null
    private val frameCount = 116
    private val fps = 30f
    private val currentColumns = 8

    private var frameWidth = 0
    private var frameHeight = 0

    private var startTime = System.currentTimeMillis()

    init {
        holder.addCallback(this)

        setZOrderOnTop(true)
        holder.setFormat(PixelFormat.TRANSLUCENT)

        val options = android.graphics.BitmapFactory.Options()
        titleBitmap = android.graphics.BitmapFactory.decodeResource(resources, R.mipmap.title_rin, options)

        titleBitmap?.let {
            frameWidth = it.width / currentColumns

            val totalRows = (frameCount + currentColumns - 1) / currentColumns

            frameHeight = it.height / totalRows
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        isRunning = true
        renderThread = Thread(this).apply { start() }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        val charWidth = 400f
        val charHeight = 522f
        val centerX = width / 2f
        val centerY = height * 0.65f

        dstRect.set(
            centerX - charWidth / 2f,
            centerY - charHeight / 2f,
            centerX + charWidth / 2f,
            centerY + charHeight / 2f
        )
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isRunning = false
        try {
            renderThread?.join()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun run() {
        while (isRunning) {
            val canvas = holder.lockCanvas() ?: continue
            try {
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

                val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000f
                val frameIndex = ((elapsedSeconds * fps).toInt()) % frameCount

                val col = frameIndex % currentColumns
                val row = frameIndex / currentColumns

                srcRect.set(
                    col * frameWidth,
                    row * frameHeight,
                    (col + 1) * frameWidth,
                    (row + 1) * frameHeight
                )

                titleBitmap?.let { bmp ->
                    canvas.drawBitmap(bmp, srcRect, dstRect, null)
                }
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }

            try { Thread.sleep(16) } catch (e: Exception) {}
        }
    }
}