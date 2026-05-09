package kr.ac.tukorea.jmw.vibecosmos.game.manager

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import kr.ac.tukorea.jmw.vibecosmos.R

class SoundManager(context: Context) {
    private val soundPool: SoundPool

    private var hitSoundId: Int = 0
    private var swingUpSoundId: Int = 0
    private var swingDownSoundId: Int = 0

    init {
        // 오디오 속성 설정
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        // SoundPool 생성
        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build()

        // 리소스 로드
        hitSoundId = soundPool.load(context, R.raw.hitsound_000, 1)
        swingUpSoundId = soundPool.load(context, R.raw.swing_up, 1)
        swingDownSoundId = soundPool.load(context, R.raw.swing_down, 1)
    }

    // 재생 함수들

    fun playHit() {
        if (hitSoundId != 0) {
            soundPool.play(hitSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }

    fun playSwingUp() {
        if (swingUpSoundId != 0) {
            soundPool.play(swingUpSoundId, 0.8f, 0.8f, 1, 0, 1.0f)
        }
    }

    fun playSwingDown() {
        if (swingDownSoundId != 0) {
            soundPool.play(swingDownSoundId, 0.8f, 0.8f, 1, 0, 1.0f)
        }
    }

    fun release() {
        soundPool.release()
    }
}