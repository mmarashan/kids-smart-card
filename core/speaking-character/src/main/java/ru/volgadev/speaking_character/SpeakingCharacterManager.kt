package ru.volgadev.speaking_character

import android.app.Activity
import android.graphics.Color
import android.graphics.PixelFormat
import android.view.View
import android.view.WindowManager
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import ru.volgadev.common.log.Logger


class SpeakingCharacterManager {

    private val logger = Logger.get("SpeakingCharacterManager")

    fun show(activity: Activity, character: Character, showPhrase: String, showTimeMs: Long) {
        logger.debug("show()")

        val context = activity.applicationContext
        val windowManager = activity.windowManager

        val imageView = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(256, 256)
            setImageDrawable(character.drawable)
        }
        val textView = TextView(context).apply {
            textSize = 22f
            text = showPhrase
        }

        val view = LinearLayout(context).apply {
            setBackgroundColor(Color.TRANSPARENT)
            orientation = LinearLayout.VERTICAL

            setOnClickListener {
                windowManager.removeViewFromOverlay(this)
            }

            addView(textView)
            addView(imageView)

            postDelayed({
                windowManager.removeViewFromOverlay(this)
            }, showTimeMs)
        }

        windowManager.showViewInOverlay(view)

        logger.debug("show(). ok")
    }

    private fun WindowManager.showViewInOverlay(view: View) {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
            PixelFormat.TRANSLUCENT
        )

        this.addView(view, params)
        view.slideDown()
        view.slideUp()
    }

    private fun WindowManager.removeViewFromOverlay(view: View) {
        // TODO: fix crash
        this.removeView(view)
    }

    private fun View.slideUp() {
        this.visibility = View.VISIBLE
        val animate = TranslateAnimation(
            0f,
            0f,
            300f,//this.height.toFloat(),
            0f
        )
        animate.duration = 500
        animate.fillAfter = true
        this.startAnimation(animate)
    }

    private fun View.slideDown() {
        val animate = TranslateAnimation(
            0f,
            0f,
            0f,
            300f // this.height.toFloat()
        )
        animate.duration = 500
        animate.fillAfter = true
        this.startAnimation(animate)
    }

    private companion object {

    }
}