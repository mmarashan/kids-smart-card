package ru.volgadev.speaking_character

import android.app.Activity
import android.content.Context
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
import ru.volgadev.common.getNavigationBarHeight
import ru.volgadev.common.getScreenSize
import ru.volgadev.common.log.Logger
import ru.volgadev.common.runSwingAnimation
import kotlin.math.roundToInt

/**
 * TODO LIST:
3. Записать видео - 0.5 ч
4. Добавить параметр "текст произнесения" - 1ч
 */
class SpeakingCharacterManager(private val context: Context) {

    private val logger = Logger.get("SpeakingCharacterManager")

    private val screenSize by lazy {
        context.getScreenSize()
    }

    private val navigationBarHeight by lazy {
        context.getNavigationBarHeight()
    }

    private val needNavBarPaddingDirections =
        setOf(Directon.FROM_BOTTOM, Directon.FROM_BOTTOM_RIGHT, Directon.FROM_BOTTOM_LEFT)

    fun show(
        activity: Activity,
        character: Character,
        showPhrase: String,
        showTimeMs: Long
    ) {
        logger.debug("show()")

        val viewWidth = character.size.width
        val viewHeight = character.size.height

        val context = activity.applicationContext
        val windowManager = activity.windowManager

        val imageView = ImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(viewWidth, viewHeight)
            setImageDrawable(character.drawable)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
        val textView = TextView(context).apply {
            val textViewWidth =
                ((character.textBound.x1 - character.textBound.x0) * viewWidth).roundToInt()
            val textViewHeight =
                ((character.textBound.y1 - character.textBound.y0) * viewHeight).roundToInt()
            setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM)
            setAutoSizeTextTypeUniformWithConfiguration(
                12, 32, 1, TypedValue.COMPLEX_UNIT_SP
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

        val direction: Directon = character.availableDirections.random()

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = direction.layoutGravity
            if (direction in needNavBarPaddingDirections) {
                verticalMargin = 1f * navigationBarHeight / screenSize.second
            }
        }

        windowManager.addView(view, params)

        val showCoeff = direction.showCoefficients
        view.children.iterator().forEach { child ->
            child.slideAndBack(
                viewWidth * showCoeff[0].toFloat(),
                viewHeight * showCoeff[1].toFloat(),
                viewWidth * showCoeff[2].toFloat(),
                viewHeight * showCoeff[3].toFloat(),
                SLIDE_ANIMATION_TIME_MS,
                showTimeMs,
                SLIDE_ANIMATION_TIME_MS
            )
        }

        logger.debug("show(). ok")
    }

    private val Directon.layoutGravity: Int
        get() {
            return when (this) {
                Directon.FROM_TOP -> (Gravity.CENTER_HORIZONTAL or Gravity.TOP)
                Directon.FROM_BOTTOM -> (Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM)
                Directon.FROM_LEFT -> (Gravity.CENTER_VERTICAL or Gravity.START)
                Directon.FROM_RIGHT -> (Gravity.CENTER_VERTICAL or Gravity.END)
                Directon.FROM_TOP_LEFT -> (Gravity.START or Gravity.TOP)
                Directon.FROM_BOTTOM_LEFT -> (Gravity.START or Gravity.BOTTOM)
                Directon.FROM_TOP_RIGHT -> (Gravity.END or Gravity.TOP)
                Directon.FROM_BOTTOM_RIGHT -> (Gravity.END or Gravity.BOTTOM)
            }
        }

    private val Directon.showCoefficients: Array<Int>
        get() {
            return when (this) {
                Directon.FROM_TOP -> arrayOf(0, 0, -1, 1)
                Directon.FROM_BOTTOM -> arrayOf(0, 0, 1, -1)
                Directon.FROM_LEFT -> arrayOf(-1, 1, 0, 0)
                Directon.FROM_RIGHT -> arrayOf(1, -1, 0, 0)
                Directon.FROM_TOP_LEFT -> arrayOf(-1, 1, -1, 1)
                Directon.FROM_BOTTOM_LEFT -> arrayOf(-1, 1, 1, -1)
                Directon.FROM_TOP_RIGHT -> arrayOf(1, -1, -1, 1)
                Directon.FROM_BOTTOM_RIGHT -> arrayOf(1, -1, 1, -1)
            }
        }

    private fun WindowManager.removeViewFromOverlay(view: View) {
        if (view.windowToken != null) this.removeView(view)
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
                    view.runSwingAnimation(2f, showTimeMs)
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