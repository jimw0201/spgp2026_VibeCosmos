package kr.ac.tukorea.jmw.vibecosmos.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.ac.tukorea.jmw.vibecosmos.game.data.SongConfig
import kr.ac.tukorea.jmw.vibecosmos.R
import kr.ac.tukorea.jmw.vibecosmos.databinding.ActivitySongSelectBinding

class SongSelectActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySongSelectBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySongSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 곡 데이터 생성
        val song1 = SongConfig("Test 1", R.raw.music1, R.mipmap.stage_bg1, "chart1.txt", 600f)
        val song2 = SongConfig("Test 2", R.raw.title, R.mipmap.stage_bg2, "chart2.txt", 900f)

        // 버튼 클릭 시 해당 곡 정보를 넘기며 게임 시작
        binding.btnSong1.setOnClickListener { startGame(song1) }
        binding.btnSong2.setOnClickListener { startGame(song2) }
    }

    private fun startGame(config: SongConfig) {
        val intent = Intent(this, VibeCosmosActivity::class.java).apply {
            putExtra("SELECTED_SONG", config)
        }
        startActivity(intent)
    }
}