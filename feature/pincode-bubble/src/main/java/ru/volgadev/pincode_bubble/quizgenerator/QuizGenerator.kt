package ru.volgadev.pincode_bubble.quizgenerator

interface QuizGenerator {
    fun getQuiz(): Pair<String, List<String>>
}