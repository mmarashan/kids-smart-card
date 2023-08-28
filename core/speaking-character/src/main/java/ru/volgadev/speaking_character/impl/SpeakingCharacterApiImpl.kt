package ru.volgadev.speaking_character.impl

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
import ru.volgadev.common.ext.dpToPx
import ru.volgadev.common.ext.getNavigationBarHeight
import ru.volgadev.common.ext.getScreenSize
import ru.volgadev.common.ext.runSwingAnimation
import ru.volgadev.speaking_character.api.Character
import ru.volgadev.speaking_character.api.Direction
import ru.volgadev.speaking_character.api.SpeakingCharacterApi
import kotlin.math.roundToInt

internal class SpeakingCharacterApiImpl(private val context: Context) : SpeakingCharacterApi {

    private val screenSize by lazy { context.getScreenSize() }

    private val navigationBarHeight by lazy { context.getNavigationBarHeight() }

    private val needNavBarPaddingDirections =
        setOf(Direction.FROM_BOTTOM, Direction.FROM_BOTTOM_RIGHT, Direction.FROM_BOTTOM_LEFT)

    var lastDirection: Direction? = null

    override fun show(
        activity: Activity,
        character: Character,
        utteranceText: String?,
        showTimeMs: Long
    ) {
        val viewWidth = context.dpToPx(character.size.widthDp)
        val viewHeight = context.dpToPx(character.size.heightDp)

        val imageView = ImageView(activity).apply {
            layoutParams = FrameLayout.LayoutParams(viewWidth, viewHeight)
            setImageResource(character.imageRes)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

        val view = FrameLayout(activity).apply {
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)

            addView(imageView)
            if (utteranceText != null) {
                val textView = TextView(activity).apply {
                    val textViewWidth =
                        ((character.textBound.x1 - character.textBound.x0) * viewWidth).roundToInt()
                    val textViewHeight =
                        ((character.textBound.y1 - character.textBound.y0) * viewHeight).roundToInt()
                    setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM)
                    setAutoSizeTextTypeUniformWithConfiguration(
                        12, 32, 1, TypedValue.COMPLEX_UNIT_SP
                    )
                    text = utteranceText
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

                addView(textView)
            }

            postDelayed({
                activity.windowManager.removeViewFromOverlay(this)
            }, showTimeMs + SLIDE_ANIMATION_TIME_MS * 2)
        }

        val direction: Direction = getNewDirection(character.availableDirections)

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

        activity.windowManager.addView(view, params)

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
    }

    private val Direction.layoutGravity: Int
        get() {
            return when (this) {
                Direction.FROM_TOP -> (Gravity.CENTER_HORIZONTAL or Gravity.TOP)
                Direction.FROM_BOTTOM -> (Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM)
                Direction.FROM_LEFT -> (Gravity.CENTER_VERTICAL or Gravity.START)
                Direction.FROM_RIGHT -> (Gravity.CENTER_VERTICAL or Gravity.END)
                Direction.FROM_TOP_LEFT -> (Gravity.START or Gravity.TOP)
                Direction.FROM_BOTTOM_LEFT -> (Gravity.START or Gravity.BOTTOM)
                Direction.FROM_TOP_RIGHT -> (Gravity.END or Gravity.TOP)
                Direction.FROM_BOTTOM_RIGHT -> (Gravity.END or Gravity.BOTTOM)
            }
        }

    private val Direction.showCoefficients: Array<Int>
        get() {
            return when (this) {
                Direction.FROM_TOP -> arrayOf(0, 0, -1, 1)
                Direction.FROM_BOTTOM -> arrayOf(0, 0, 1, -1)
                Direction.FROM_LEFT -> arrayOf(-1, 1, 0, 0)
                Direction.FROM_RIGHT -> arrayOf(1, -1, 0, 0)
                Direction.FROM_TOP_LEFT -> arrayOf(-1, 1, -1, 1)
                Direction.FROM_BOTTOM_LEFT -> arrayOf(-1, 1, 1, -1)
                Direction.FROM_TOP_RIGHT -> arrayOf(1, -1, -1, 1)
                Direction.FROM_BOTTOM_RIGHT -> arrayOf(1, -1, 1, -1)
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

    private fun getNewDirection(availableDirections: Set<Direction>): Direction {
        if (availableDirections.isEmpty()) lastDirection = Direction.FROM_BOTTOM
        if (availableDirections.size == 1) lastDirection = availableDirections.first()
        lastDirection =
            availableDirections.filter { direction -> direction != lastDirection }.random()
        return lastDirection!!
    }

    private companion object {
        const val SLIDE_ANIMATION_TIME_MS = 600L
    }
}