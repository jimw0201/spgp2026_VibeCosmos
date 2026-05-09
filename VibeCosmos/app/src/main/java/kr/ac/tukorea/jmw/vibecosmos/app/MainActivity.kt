package kr.ac.tukorea.jmw.vibecosmos.app

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kr.ac.tukorea.jmw.vibecosmos.R
import kr.ac.tukorea.jmw.vibecosmos.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    // BGM 제어 변수
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        mediaPlayer = MediaPlayer.create(this, R.raw.title).apply {
            isLooping = true
            start()
        }
    }

    fun onBtnStartGame(view: View) {
        mediaPlayer?.stop()

        val intent = Intent(this, SongSelectActivity::class.java)
        startActivity(intent)
    }

    private fun startGameActivity() {
        val intent = Intent(this, VibeCosmosActivity::class.java)
        startActivity(intent)
    }

    // 액티비티가 화면에서 사라질 때
    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
    }

    // 다시 앱으로 돌아왔을 때
    override fun onResume() {
        super.onResume()
        mediaPlayer?.start()
    }

    // 액티비티가 완전히 종료될 때
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}