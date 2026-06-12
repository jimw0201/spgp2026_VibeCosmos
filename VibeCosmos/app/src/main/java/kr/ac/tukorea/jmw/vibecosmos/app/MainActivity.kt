package kr.ac.tukorea.jmw.vibecosmos.app

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kr.ac.tukorea.jmw.vibecosmos.R
import kr.ac.tukorea.jmw.vibecosmos.databinding.ActivityMainBinding
import kr.ac.tukorea.jmw.vibecosmos.game.main.TitleCharacterView
import kr.ac.tukorea.jmw.vibecosmos.game.manager.SongCatalog

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        SongCatalog.load(this.assets)

        val titleCharView = TitleCharacterView(this)
        binding.titleCharacterContainer.addView(titleCharView)
    }

    fun onBtnStartGame(view: View) {
        stopAndReleaseMediaPlayer()

        val intent = Intent(this, SongSelectActivity::class.java)
        startActivity(intent)
    }

    private fun stopAndReleaseMediaPlayer() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
    }

    override fun onResume() {
        super.onResume()
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.title).apply {
                isLooping = true
                start()
            }
        } else if (!mediaPlayer!!.isPlaying) {
            mediaPlayer!!.start()
        }
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAndReleaseMediaPlayer()
    }
}