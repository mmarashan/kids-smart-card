package ru.volgadev.common.logger

class TestLoggerDelegate: LoggerDelegate {
    override fun debug(tag: String, m: String) = println("$tag $m")

    override fun info(tag: String, m: String) = println("$tag $m")

    override fun warn(tag: String, m: String) = println("$tag $m")

    override fun error(tag: String, m: String) = println("$tag $m")
}