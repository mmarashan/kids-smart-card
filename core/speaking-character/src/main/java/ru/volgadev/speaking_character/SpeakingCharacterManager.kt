package ru.volgadev.speaking_character

import android.app.Activity
import android.graphics.Color
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import ru.volgadev.common.log.Logger


class SpeakingCharacterManager {

    private val logger = Logger.get("SpeakingCharacterManager")

    fun show(activity: Activity, character: Character, showPhrase: String, showTimeMs: Long) {
        logger.debug("show()")

        val imageSizePx = 360

        val context = activity.applicationContext
        val windowManager = activity.windowManager

        val imageView = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(imageSizePx, imageSizePx)
            setImageDrawable(character.drawable)
        }
        val textView = TextView(context).apply {
            textSize = 24f
            text = showPhrase
        }

        val view = LinearLayout(context).apply {
            setBackgroundColor(Color.TRANSPARENT)
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            setPadding(PADDING_PX, PADDING_PX, PADDING_PX, PADDING_PX)

            addView(textView)
            addView(imageView)

            postDelayed({
                windowManager.removeViewFromOverlay(this)
            }, showTimeMs + SLIDE_ANIMATION_TIME_MS * 2)
        }

        windowManager.showViewInOverlay(view)
        textView.slideAndBack(
            -imageSizePx.toFloat(),
            imageSizePx.toFloat(),
            -imageSizePx.toFloat(),
            imageSizePx.toFloat(),
            SLIDE_ANIMATION_TIME_MS,
            showTimeMs,
            SLIDE_ANIMATION_TIME_MS
        )
        imageView.slideAndBack(
            -imageSizePx.toFloat(),
            imageSizePx.toFloat(),
            -imageSizePx.toFloat(),
            imageSizePx.toFloat(),
            SLIDE_ANIMATION_TIME_MS,
            showTimeMs,
            SLIDE_ANIMATION_TIME_MS
        )

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
            gravity = (Gravity.START or Gravity.TOP)
         }

        this.addView(view, params)
    }

    private fun WindowManager.removeViewFromOverlay(view: View) {
        // TODO: fix crash
        this.removeView(view)
    }

    private fun View.slideAndBack(
        xStart: Float,
        dx: Float,
        yStart: Float,
        dy: Float,
        animInMs: Long,
        showTimeMs: Long,
        animOutMs: Long
    ) {
        val view = this
        val translateAnimation = TranslateAnimation(
            xStart,
            dx + xStart,
            yStart,
            dy + yStart
        ).apply {
            duration = animInMs
            fillAfter = true
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                    view.postDelayed({
                        val translateAnimationBack = TranslateAnimation(
                            dx + xStart,
                            xStart,
                            dy + yStart,
                            yStart
                        ).apply {
                            duration = animOutMs
                            fillAfter = true
                        }
                        view.startAnimation(translateAnimationBack)
                    }, showTimeMs)
                }

                override fun onAnimationRepeat(animation: Animation?) {
                }

            })
        }
        view.startAnimation(translateAnimation)
    }

    private companion object {
        const val SLIDE_ANIMATION_TIME_MS = 500L
        const val PADDING_PX = 64
    }
}