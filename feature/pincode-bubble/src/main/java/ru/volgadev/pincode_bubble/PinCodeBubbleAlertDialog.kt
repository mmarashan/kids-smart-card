package ru.volgadev.pincode_bubble

import android.app.Activity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.MainThread
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import ru.volgadev.common.log.Logger

@MainThread
class PinCodeBubbleAlertDialog(
    private val activity: Activity,
    private val title: String,
    private val question: String,
    private val answers: Collection<String>
) : AlertDialog(activity, false, null) {

    private val logger = Logger.get("PinCodeBubbleAlertDialog")

    private val view = activity.layoutInflater
        .inflate(R.layout.dialog, null) as LinearLayout

    private val questionTextView = view.findViewById<TextView>(R.id.questionTextView)
    private val answerEditText = view.findViewById<EditText>(R.id.answerEditText)
    private val okBtn = view.findViewById<Button>(R.id.okBtn)

    init {
        setTitle(title)
        setView(view)
        setCancelable(false)
        questionTextView.text = question
    }

    fun showForResult(): Flow<Boolean> {
        logger.debug("showForResult()")
        val channel = Channel<Boolean>()
        okBtn.setOnClickListener {
            logger.debug("on click OK")
            val answer = answerEditText.text.toString()
            val isCorrectAnswer = answers.contains(answer)
            logger.debug("answer = $answer; Result = $isCorrectAnswer")
            channel.offer(isCorrectAnswer)
            channel.close()
        }
        show()
        return channel.consumeAsFlow()
    }

    override fun show() {
        logger.warn("You should call showForResult() for handle user answer")
        super.show()
    }
}