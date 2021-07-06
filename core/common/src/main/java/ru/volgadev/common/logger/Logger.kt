package ru.volgadev.common.logger

interface LoggerDelegate {
    fun debug(tag: String, m: String)
    fun info(tag: String, m: String)
    fun warn(tag: String, m: String)
    fun error(tag: String, m: String)
}

class Logger(private val tag: String,
             private val delegates: List<LoggerDelegate>) {

    fun debug(m: String) {
        delegates.forEach {delegate -> delegate.debug(tag, m) }
    }

    fun info(m: String) {
        delegates.forEach {delegate -> delegate.info(tag, m) }
    }

    fun warn(m: String) {
        delegates.forEach {delegate -> delegate.warn(tag, m) }
    }

    fun error(m: String) {
        delegates.forEach {delegate -> delegate.error(tag, m) }
    }

    companion object {

        @Volatile
        private var delegates: List<LoggerDelegate> = listOf()

        fun setDelegates(vararg delegates: LoggerDelegate) {
            if (delegates.isEmpty()){
                throw AssertionError("Empty delegates")
            }
            Companion.delegates = delegates.toList()
        }

        fun get(tag: String): Logger {
            return Logger(tag, delegates)
        }
    }

}