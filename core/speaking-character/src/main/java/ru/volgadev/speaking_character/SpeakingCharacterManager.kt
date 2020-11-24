package ru.volgadev.speaking_character

import android.app.Activity
import android.graphics.Color
import android.graphics.PixelFormat
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import ru.volgadev.common.log.Logger
import kotlin.math.roundToInt

/**
 * TODO LIST:
 * 1. Рандомные направления выхода - 1ч
 * 2. Облачко позади текста - кординаты расположения текста - 2ч
 * 3. Анимация легкого вращения - 30 мин
 */
class SpeakingCharacterManager {

    private val logger = Logger.get("SpeakingCharacterManager")

    fun show(activity: Activity, character: Character, showPhrase: String, showTimeMs: Long) {
        logger.debug("show()")

        val imageSizePx = 360
        val viewWidth = imageSizePx
        val viewHeight = imageSizePx

        val context = activity.applicationContext
        val windowManager = activity.windowManager

        val imageView = ImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(imageSizePx, imageSizePx)
            setImageDrawable(character.drawable)
        }
        val textView = TextView(context).apply {
            val textViewWidth =
                ((character.textBound.x1 - character.textBound.x0) * viewWidth).roundToInt()
            val textViewHeight =
                ((character.textBound.y1 - character.textBound.y0) * viewHeight).roundToInt()
            setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM)
            setAutoSizeTextTypeUniformWithConfiguration(
                16, 24, 1, TypedValue.COMPLEX_UNIT_SP
            )
            text = showPhrase
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(textViewWidth, textViewHeight).apply {
                setMargins(
                    (viewWidth * character.textBound.x0).roundToInt(),
                    (viewHeight * character.textBound.y0).roundToInt(),
                    (viewWidth - viewWidth * character.textBound.x1).roundToInt(),
                    (viewHeight - viewHeight * character.textBound.y1).roundToInt()
                )
            }
        }

        val view = FrameLayout(context).apply {
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)

            addView(imageView)
            addView(textView)

            postDelayed({
                windowManager.removeViewFromOverlay(this)
            }, showTimeMs + SLIDE_ANIMATION_TIME_MS * 2)
        }

        windowManager.showViewInOverlay(view)
        view.children.iterator().forEach { child ->
            child.slideAndBack(
                0f,
                0f,
                imageSizePx.toFloat(),
                -1f * imageSizePx,
                SLIDE_ANIMATION_TIME_MS,
                showTimeMs,
                SLIDE_ANIMATION_TIME_MS
            )
        }

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
            gravity = (Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM)
            verticalMargin = 0.1f
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