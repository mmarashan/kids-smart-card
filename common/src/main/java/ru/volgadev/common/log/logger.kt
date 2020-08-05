package ru.volgadev.common.log

interface LoggerDelegate {
    fun debug(TAG: String, m: String)
    fun info(TAG: String, m: String)
    fun warn(TAG: String, m: String)
    fun error(TAG: String, m: String)
}

class Logger(private val TAG: String,
             private val delegates: List<LoggerDelegate>) {

    fun debug(m: String) {
        delegates.forEach {delegate -> delegate.debug(TAG, m) }
    }

    fun info(m: String) {
        delegates.forEach {delegate -> delegate.info(TAG, m) }
    }

    fun warn(m: String) {
        delegates.forEach {delegate -> delegate.warn(TAG, m) }
    }

    fun error(m: String) {
        delegates.forEach {delegate -> delegate.error(TAG, m) }
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

        fun get(TAG: String): Logger {
            return Logger(TAG, delegates)
        }
    }

}