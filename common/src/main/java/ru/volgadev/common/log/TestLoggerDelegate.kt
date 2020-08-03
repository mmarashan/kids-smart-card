package ru.volgadev.common.log

class TestLoggerDelegate(val TAG: String) : LoggerDelegate {
    override fun debug(m: String) {
        println("$TAG $m")
    }

    override fun info(m: String) {
        println("$TAG $m")
    }

    override fun warn(m: String) {
        println("$TAG $m")
    }

    override fun error(m: String) {
        println("$TAG $m")
    }
}