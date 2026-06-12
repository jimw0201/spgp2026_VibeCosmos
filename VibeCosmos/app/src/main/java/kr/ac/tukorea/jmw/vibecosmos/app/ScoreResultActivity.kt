package kr.ac.tukorea.jmw.vibecosmos.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.ac.tukorea.jmw.vibecosmos.databinding.ActivityScoreResultBinding

class ScoreResultActivity : AppCompatActivity() {
    private val binding by lazy { ActivityScoreResultBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val finalScore = intent.getIntExtra("FINAL_SCORE", 0)
        val maxCombo = intent.getIntExtra("MAX_COMBO", 0)

        binding.tvFinalScore.text = String.format("%,d", finalScore)
        binding.tvMaxCombo.text = maxCombo.toString()

        binding.btnRetry.setOnClickListener {
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
            val intent = Intent(this, SongSelectActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }
}