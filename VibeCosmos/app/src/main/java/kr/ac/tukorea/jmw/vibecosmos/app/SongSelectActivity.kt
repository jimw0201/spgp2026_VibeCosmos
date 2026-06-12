package kr.ac.tukorea.jmw.vibecosmos.app

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.ac.tukorea.jmw.vibecosmos.game.data.SongConfig
import kr.ac.tukorea.jmw.vibecosmos.R
import kr.ac.tukorea.jmw.vibecosmos.databinding.ActivitySongSelectBinding
import kr.ac.tukorea.jmw.vibecosmos.game.manager.SoundManager

class SongSelectActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySongSelectBinding
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySongSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 곡 데이터 생성 (제목, 음원, 배경, 채보 파일명, 속도)
        val song1 = SongConfig("Fly↑High", "music1", "stage_bg1", "chart1.txt", 600f)
        val song2 = SongConfig("Test 2", "title", "stage_bg2", "chart2.txt", 900f)

        // 각각의 곡 선택 버튼 클릭 시 해당 곡 정보를 넘기며 게임 화면으로 이동
        binding.btnSong1.setOnClickListener { startGame(song1) }
        binding.btnSong2.setOnClickListener { startGame(song2) }
    }

    // 게임 액티비티를 시작하는 함수
    private fun startGame(config: SongConfig) {
        stopAndReleaseMediaPlayer()
        val intent = Intent(this, VibeCosmosActivity::class.java).apply {
            putExtra("SELECTED_SONG", config)
        }
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
            mediaPlayer = MediaPlayer.create(this, R.raw.list).apply {
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