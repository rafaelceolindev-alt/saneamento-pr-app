package com.example.saneamentopr

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val dropIcon = findViewById<ImageView>(R.id.dropIcon)
        val textSplash = findViewById<TextView>(R.id.textSplash)

        // 🔹 Animação de "preenchimento" da gota
        val scaleUp = ScaleAnimation(
            0.3f, 1f, 0.3f, 1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        scaleUp.duration = 1800
        scaleUp.fillAfter = true

        // 🔹 Efeito de brilho / fade-in no texto
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = 2000
        fadeIn.fillAfter = true

        dropIcon.startAnimation(scaleUp)
        textSplash.startAnimation(fadeIn)

        // ⏱️ Delay antes de ir para a MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 2500)
    }
}

