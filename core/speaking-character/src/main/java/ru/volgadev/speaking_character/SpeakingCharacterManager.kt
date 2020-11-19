package ru.volgadev.speaking_character

import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import ru.volgadev.common.log.Logger


class SpeakingCharacterManager {

    private val logger = Logger.get("SpeakingCharacterManager")

    fun show(activity: Activity, character: Character, text: String, showTimeMs: Long) {
        logger.debug("show()")

        val view: View = LayoutInflater.from(activity).inflate(R.layout.character_alert_dialog, null)
        val imageView = view.findViewById<ImageView>(R.id.dialog_imageview)
        val textView = view.findViewById<TextView>(R.id.dialog_text)
        textView.text = text
        imageView.setImageDrawable(character.drawable)

        view.setOnClickListener {
            removeOverlay(activity, view)
        }

        showOverlay(activity, view)
    }

    fun showOverlay(activity: Activity, overlay: View) {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
            PixelFormat.TRANSLUCENT
        )
        activity.windowManager.addView(overlay, params)
    }

    fun removeOverlay(activity: Activity, overlay: View) {
        activity.windowManager.removeView(overlay)
    }

    private companion object {

    }
}