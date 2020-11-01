package ru.volgadev.pincode_bubble

import android.app.Activity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.MainThread
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.volgadev.common.hideNavBar
import ru.volgadev.common.log.Logger

@MainThread
class PinCodeBubbleAlertDialog(
    private val activity: Activity,
    private val title: String,
    private val question: String,
    private val answers: Collection<String>,
    private val hideNavigationBar: Boolean = false
) : AlertDialog(activity, false, null) {

    private val logger = Logger.get("PinCodeBubbleAlertDialog")

    private val view = activity.layoutInflater
        .inflate(R.layout.dialog, null) as LinearLayout

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

    fun showForResult(): LiveData<Boolean> {
        logger.debug("showForResult()")
        val resultLiveData = MutableLiveData<Boolean>()
        okBtn.setOnClickListener {
            logger.debug("on click OK")
            val answer = answerEditText.text.toString()
            val isCorrectAnswer = answers.contains(answer)
            logger.debug("Answer = $answer; Result = $isCorrectAnswer")
            resultLiveData.value = isCorrectAnswer
            dismiss()
        }
        cancelBtn.setOnClickListener {
            logger.debug("on click cancel")
            resultLiveData.value =  false
            hide()
            dismiss()
        }
        show()
        return resultLiveData
    }

    override fun show() {
        logger.debug("show()")
        super.show()
        if (hideNavigationBar) window?.hideNavBar()
    }

    override fun onStop() {
        logger.debug("onStop()")
        super.onStop()
        if (hideNavigationBar) window?.hideNavBar()
    }
}