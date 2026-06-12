package kr.ac.tukorea.jmw.vibecosmos.app

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.activity.OnBackPressedCallback // ★ 추가
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import kr.ac.tukorea.jmw.vibecosmos.R
import kr.ac.tukorea.jmw.vibecosmos.databinding.ActivityMainBinding
import kr.ac.tukorea.jmw.vibecosmos.game.manager.SongCatalog
import kr.ac.tukorea.jmw.vibecosmos.app.TitleCharacterView
import kr.ac.tukorea.jmw.vibecosmos.game.manager.SoundManager

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private var mediaPlayer: MediaPlayer? = null

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
                binding.drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        SongCatalog.load(this.assets)

        val titleCharView = TitleCharacterView(this)
        binding.titleCharacterContainer.addView(titleCharView)

        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        binding.btnSettings.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.END)
            backPressedCallback.isEnabled = true
        }

        binding.sbBgmVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val volume = progress / 100f

                mediaPlayer?.setVolume(volume, volume)

                SoundManager.bgmVolume = volume
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.sbSfxVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val volume = progress / 100f

                SoundManager.sfxVolume = volume
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
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
                val currentVol = binding.sbBgmVolume.progress / 100f
                setVolume(currentVol, currentVol)
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