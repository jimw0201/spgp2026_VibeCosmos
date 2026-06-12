package kr.ac.tukorea.jmw.vibecosmos.app

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.ac.tukorea.jmw.vibecosmos.R
import kr.ac.tukorea.jmw.vibecosmos.databinding.ActivityScoreResultBinding
import kr.ac.tukorea.jmw.vibecosmos.game.manager.SoundManager

class ScoreResultActivity : AppCompatActivity() {
    private val binding by lazy { ActivityScoreResultBinding.inflate(layoutInflater) }
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val finalScore = intent.getIntExtra("FINAL_SCORE", 0)
        val maxCombo = intent.getIntExtra("MAX_COMBO", 0)

        val perfectCount = intent.getIntExtra("PERFECT_COUNT", 0)
        val greatCount = intent.getIntExtra("GREAT_COUNT", 0)
        val missCount = intent.getIntExtra("MISS_COUNT", 0)

        binding.tvFinalScore.text = String.format("%,d", finalScore)
        binding.tvMaxCombo.text = maxCombo.toString()

        binding.tvPerfectCount.text = perfectCount.toString()
        binding.tvGreatCount.text = greatCount.toString()
        binding.tvMissCount.text = missCount.toString()

        binding.btnRetry.setOnClickListener {
            stopAndReleaseMediaPlayer()

            val songConfig = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra("SELECTED_SONG", kr.ac.tukorea.jmw.vibecosmos.game.data.SongConfig::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getSerializableExtra("SELECTED_SONG") as? kr.ac.tukorea.jmw.vibecosmos.game.data.SongConfig
            }

            val nextIntent = if (songConfig != null) {
                Intent(this, VibeCosmosActivity::class.java).apply {
                    putExtra("SELECTED_SONG", songConfig)
                }
            } else {
                Intent(this, SongSelectActivity::class.java)
            }

            nextIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(nextIntent)
            finish()
        }

        binding.btnSongSelect.setOnClickListener {
            stopAndReleaseMediaPlayer()
            val intent = Intent(this, SongSelectActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
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
            mediaPlayer = MediaPlayer.create(this, R.raw.result).apply {
                isLooping = true
                val currentVol = SoundManager.bgmVolume
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