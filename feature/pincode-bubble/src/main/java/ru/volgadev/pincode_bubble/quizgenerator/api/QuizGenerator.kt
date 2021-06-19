package ru.volgadev.pincode_bubble.quizgenerator.api

interface QuizGenerator {
    fun getQuiz(): Quiz
}

data class Quiz(
    val question: String,
    val answers: Collection<String>
)