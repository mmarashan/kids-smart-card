package ru.volgadev.pincode_bubble.quizgenerator.impl

import android.content.Context
import ru.volgadev.pincode_bubble.R
import ru.volgadev.pincode_bubble.quizgenerator.api.Quiz
import ru.volgadev.pincode_bubble.quizgenerator.api.QuizGenerator

class NumbersAdditionQuizGenerator(private val context: Context) : QuizGenerator {

    override fun getQuiz(): Quiz {
        val int1 = (10..50).random()
        val int2 = (10..50).random()
        val answer = int1 + int2
        val questionText = context.getString(R.string.numbers_addition_question, int1, int2)
        return Quiz(questionText, listOf(answer.toString()))
    }
}