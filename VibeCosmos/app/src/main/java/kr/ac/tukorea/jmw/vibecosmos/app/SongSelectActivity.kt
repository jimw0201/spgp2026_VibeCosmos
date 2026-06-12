package kr.ac.tukorea.jmw.vibecosmos.app

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.ac.tukorea.jmw.vibecosmos.game.data.SongConfig
import kr.ac.tukorea.jmw.vibecosmos.R
import kr.ac.tukorea.jmw.vibecosmos.databinding.ActivitySongSelectBinding
import kr.ac.tukorea.jmw.vibecosmos.game.manager.SongCatalog
import kr.ac.tukorea.jmw.vibecosmos.game.manager.SoundManager

class SongSelectActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySongSelectBinding
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySongSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (SongCatalog.songs.isEmpty()) {
            SongCatalog.load(this.assets)
        }

        val songList = SongCatalog.songs

        if (songList.isNotEmpty()) {
            if (songList.size > 0) {
                val song1 = songList[0]
                binding.btnSong1.text = song1.title
                binding.btnSong1.setOnClickListener { startGame(song1) }
            }

            if (songList.size > 1) {
                val song2 = songList[1]
                binding.btnSong2.text = song2.title
                binding.btnSong2.setOnClickListener { startGame(song2) }
            }
        } else {
            android.util.Log.e("SONG_DEBUG", "songs.json 로드 실패 혹은 파싱 실패로 데이터가 비어있습니다!")
        }
    }

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