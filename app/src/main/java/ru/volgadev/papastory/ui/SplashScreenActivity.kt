package ru.volgadev.papastory.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler

import androidx.appcompat.app.AppCompatActivity

private const val SPLASH_DISPLAY_DURATION = 1000L

class SplashScreenActivity : AppCompatActivity() {

    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        Handler().postDelayed({
            val mainIntent = Intent(this@SplashScreenActivity, MainActivity::class.java)
            this@SplashScreenActivity.startActivity(mainIntent)
            finish()
        }, SPLASH_DISPLAY_DURATION)
    }
}
