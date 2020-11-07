package ru.volgadev.pincode_bubble.quizgenerator

import android.content.Context
import ru.volgadev.pincode_bubble.R

class NumbersAdditionQuizGenerator(private val context: Context) : QuizGenerator {

    override fun getQuiz(): Pair<String, List<String>> {
        val int1 = (10..50).random()
        val int2 = (10..50).random()
        val answer = int1 + int2
        val questionText = context.getString(R.string.numbers_addition_question, int1, int2)
        return Pair(questionText, listOf(answer.toString()))
    }
}