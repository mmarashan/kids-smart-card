package ru.volgadev.livebanner.common.log

interface LoggerDelegate {
    fun debug(m: String)
    fun info(m: String)
    fun warn(m: String)
    fun error(m: String)
}

class Logger(private val delegate: LoggerDelegate) {

    fun debug(m: String){
        delegate.debug(m)
    }
    fun info(m: String){
        delegate.info(m)
    }
    fun warn(m: String){
        delegate.warn(m)
    }
    fun error(m: String){
        delegate.error(m)
    }

    companion object {
        fun get(TAG: String, delegate: LoggerDelegate = AndroidLoggerDelegate(TAG) ): Logger {
            return Logger(delegate)
        }
    }

}