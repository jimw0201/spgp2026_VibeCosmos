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
    private var damageSoundId: Int = 0
    private var readyGoSoundId: Int = 0

    companion object {
        var bgmVolume: Float = 0.7f
        var sfxVolume: Float = 0.8f
    }

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build()

        hitSoundId = soundPool.load(context, R.raw.hitsound_000, 1)
        swingUpSoundId = soundPool.load(context, R.raw.swing_up, 1)
        swingDownSoundId = soundPool.load(context, R.raw.swing_down, 1)
        damageSoundId = soundPool.load(context, R.raw.damage, 1)
        readyGoSoundId = soundPool.load(context, R.raw.readygo, 1)
    }

    fun playHit() {
        if (hitSoundId != 0) {
            val vol = 1.0f * sfxVolume
            soundPool.play(hitSoundId, vol, vol, 1, 0, 1.0f)
        }
    }

    fun playDamage() {
        if (damageSoundId != 0) {
            val vol = 1.0f * sfxVolume
            soundPool.play(damageSoundId, vol, vol, 1, 0, 1.0f)
        }
    }

    fun playSwingUp() {
        if (swingUpSoundId != 0) {
            val vol = 0.8f * sfxVolume
            soundPool.play(swingUpSoundId, vol, vol, 1, 0, 1.0f)
        }
    }

    fun playSwingDown() {
        if (swingDownSoundId != 0) {
            val vol = 0.8f * sfxVolume
            soundPool.play(swingDownSoundId, vol, vol, 1, 0, 1.0f)
        }
    }

    fun playReadyGo() {
        if (readyGoSoundId != 0) {
            val vol = 1.0f * sfxVolume
            soundPool.play(readyGoSoundId, vol, vol, 1, 0, 1.0f)
        }
    }

    fun release() {
        soundPool.release()
    }
}