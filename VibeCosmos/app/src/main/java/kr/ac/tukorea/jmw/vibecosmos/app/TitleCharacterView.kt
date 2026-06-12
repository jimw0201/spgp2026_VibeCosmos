package kr.ac.tukorea.jmw.vibecosmos.app

import android.content.Context
import android.graphics.ImageDecoder
import android.graphics.PixelFormat
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import kr.ac.tukorea.jmw.vibecosmos.R
import java.io.IOException

class TitleCharacterView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    private var animatedDrawable: AnimatedImageDrawable? = null

    init {
        holder.addCallback(this)

        setZOrderOnTop(true)
        holder.setFormat(PixelFormat.TRANSLUCENT)

        setWillNotDraw(false)

        loadGifAsync()
    }

    private fun loadGifAsync() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Thread {
                try {
                    val source = ImageDecoder.createSource(resources, R.drawable.title_rin)
                    val drawable = ImageDecoder.decodeDrawable(source)

                    if (drawable is AnimatedImageDrawable) {
                        animatedDrawable = drawable

                        animatedDrawable?.repeatCount = AnimatedImageDrawable.REPEAT_INFINITE

                        post {
                            animatedDrawable?.start()
                            invalidate()
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }.start()
        }
    }

    override fun onDraw(canvas: android.graphics.Canvas) {
        super.onDraw(canvas)

        animatedDrawable?.let { drawable ->
            drawable.setBounds(0, 0, width, height)
            drawable.draw(canvas)

            invalidate()
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {}

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        val params = layoutParams as? FrameLayout.LayoutParams ?: return
        val charWidth = 865
        val charHeight = 946

        params.width = charWidth
        params.height = charHeight
        params.gravity = android.view.Gravity.TOP or android.view.Gravity.LEFT

        val targetLeftMargin = (width * 0.25f) - (charWidth / 2f)

        params.leftMargin = Math.max(0, targetLeftMargin.toInt())

        val baseBottomMargin = 100
        params.topMargin = height - charHeight - baseBottomMargin

        layoutParams = params
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            animatedDrawable?.stop()
        }
        animatedDrawable = null
    }
}