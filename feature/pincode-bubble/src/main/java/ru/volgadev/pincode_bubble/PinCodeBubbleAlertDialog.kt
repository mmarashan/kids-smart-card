package ru.volgadev.pincode_bubble

import android.app.Activity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.MainThread
import androidx.appcompat.app.AlertDialog
import ru.volgadev.common.log.Logger

@MainThread
class PinCodeBubbleAlertDialog(
    private val activity: Activity,
    private val title: String,
    private val question: String,
    private val answers: Collection<String>
) : AlertDialog(activity, false, null) {

    private val logger = Logger.get("PinCodeBubbleAlertDialog")

    init {
        val view = activity.layoutInflater
            .inflate(R.layout.dialog, null) as LinearLayout

        setTitle(title)
        setView(view)
        setCancelable(false)

        val questionTextView = view.findViewById<TextView>(R.id.questionTextView)
        val answerEditText = view.findViewById<EditText>(R.id.answerEditText)
        val okBtn = view.findViewById<Button>(R.id.okBtn)

        questionTextView.text = question
        okBtn.setOnClickListener {
            logger.debug("on click OK")
            val answer = answerEditText.text.toString()
            val isCorrectAnswer = answers.contains(answer)

            logger.debug("answer = $answer; Result = $isCorrectAnswer")
        }
    }
}