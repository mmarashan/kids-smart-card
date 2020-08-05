package ru.volgadev.common.log

class TestLoggerDelegate: LoggerDelegate {
    override fun debug(TAG: String, m: String) {
        println("$TAG $m")
    }

    override fun info(TAG: String, m: String) {
        println("$TAG $m")
    }

    override fun warn(TAG: String, m: String) {
        println("$TAG $m")
    }

    override fun error(TAG: String, m: String) {
        println("$TAG $m")
    }
}