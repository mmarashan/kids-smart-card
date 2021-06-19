package ru.volgadev.pincode_bubble

import android.app.Activity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import ru.volgadev.common.ext.hideNavBar
import ru.volgadev.pincode_bubble.quizgenerator.api.QuizGenerator

class PinCodeBubbleAlertDialog private constructor(
    activity: Activity,
    title: String,
    question: String,
    private val answers: Collection<String>,
    private val hideNavigationBar: Boolean = false
) : AlertDialog(activity, false, null) {

    private val view = layoutInflater.inflate(R.layout.dialog, null) as LinearLayout

    private val questionTextView = view.findViewById<TextView>(R.id.questionTextView)
    private val answerEditText = view.findViewById<EditText>(R.id.answerEditText)
    private val okBtn = view.findViewById<Button>(R.id.okBtn)
    private val cancelBtn = view.findViewById<Button>(R.id.cancelBtn)

    init {
        setTitle(title)
        setView(view)
        setCancelable(false)
        questionTextView.text = question
    }

    suspend fun showForResult(): Flow<Boolean> = callbackFlow {
        okBtn.setOnClickListener {
            val answer = answerEditText.text.toString()
            val isCorrectAnswer = answers.contains(answer)
            sendBlocking(isCorrectAnswer)
            dismiss()
        }
        cancelBtn.setOnClickListener {
            hide()
            dismiss()
            cancel()
        }
        answerEditText.requestFocus()
        show()

        awaitClose {
            hide()
            dismiss()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (hideNavigationBar) window?.hideNavBar()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (hideNavigationBar) window?.hideNavBar()
    }

    companion object {

        fun create(
            activity: Activity,
            title: String,
            quizGenerator: QuizGenerator,
            hideNavigationBar: Boolean
        ): PinCodeBubbleAlertDialog {
            val quiz = quizGenerator.getQuiz()
            return PinCodeBubbleAlertDialog(
                activity,
                title,
                quiz.question,
                quiz.answers,
                hideNavigationBar
            )
        }
    }
}