package ru.volgadev.speaking_character

import android.app.Activity
import android.graphics.Color
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
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
            layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)

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
        textView.slide(256f, 0f, 500L, -256f, 0f)
        imageView.slide(256f, 0f, 500L, -256f, 0f)

        textView.slide(-256f, 0f, 500L, 0f, 0f)
        imageView.slide(-256f, 0f, 500L, 0f, 0f)

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
        ).apply {
            gravity = Gravity.START
        }

        this.addView(view, params)
    }

    private fun WindowManager.removeViewFromOverlay(view: View) {
        // TODO: fix crash
        this.removeView(view)
    }

    private fun View.slide(
        dx: Float,
        dy: Float,
        durationMs: Long,
        dxStart: Float = 0f,
        dyStart: Float = 0f
    ) {
        val translateAnimation = TranslateAnimation(
            dxStart,
            dx + dxStart,
            dyStart,
            dy + dyStart
        ).apply {
            duration = durationMs
            fillAfter = true
        }
        this.startAnimation(translateAnimation)
    }

    private companion object {

    }
}