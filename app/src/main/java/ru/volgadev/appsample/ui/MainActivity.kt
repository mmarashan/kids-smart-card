package ru.volgadev.appsample.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.volgadev.appsample.R
import ru.volgadev.common.log.Logger
import ru.volgadev.article_galery.ui.ArticleGaleryFragment

class MainActivity : AppCompatActivity() {

    private val logger = Logger.get("MainActivity")

    override fun onCreate(savedInstanceState: Bundle?) {
        logger.debug("On create")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, ArticleGaleryFragment.newInstance())
                    .commitNow()
        }
    }
}