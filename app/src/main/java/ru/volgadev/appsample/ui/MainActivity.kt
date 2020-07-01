package ru.volgadev.appsample.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.volgadev.appsample.R
import ru.volgadev.samplefeature.main.SampleFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, SampleFragment.newInstance())
                    .commitNow()
        }
    }
}