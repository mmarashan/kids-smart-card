package ru.volgadev.common.test

import android.os.Bundle
import android.widget.FrameLayout
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import ru.volgadev.common.R

@VisibleForTesting
class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val frameLayout = FrameLayout(this)
        frameLayout.id = R.id.container
        setContentView(frameLayout)
    }
}