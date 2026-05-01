package kr.ac.tukorea.jmw.vibecosmos.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kr.ac.tukorea.jmw.vibecosmos.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    fun onBtnStartGame(view: View) {
        startGameActivity()
    }

    private fun startGameActivity() {
        val intent = Intent(this, VibeCosmosActivity::class.java)
        startActivity(intent)
    }
}