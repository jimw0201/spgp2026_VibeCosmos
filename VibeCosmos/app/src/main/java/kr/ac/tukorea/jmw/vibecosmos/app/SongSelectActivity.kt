package kr.ac.tukorea.jmw.vibecosmos.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.ac.tukorea.jmw.vibecosmos.game.data.SongConfig
import kr.ac.tukorea.jmw.vibecosmos.R
import kr.ac.tukorea.jmw.vibecosmos.databinding.ActivitySongSelectBinding // 바인딩 클래스 추가

class SongSelectActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySongSelectBinding // 바인딩 변수 선언

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 뷰 바인딩 초기화
        binding = ActivitySongSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. 곡 데이터 생성
        val song1 = SongConfig("Starry Night", R.raw.title, R.mipmap.stage_bg1, "chart1.txt", 600f)
        val song2 = SongConfig("Cyber City", R.raw.title, R.mipmap.stage_bg2, "chart2.txt", 900f)

        // 2. 버튼 클릭 시 해당 곡 정보를 넘기며 게임 시작
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