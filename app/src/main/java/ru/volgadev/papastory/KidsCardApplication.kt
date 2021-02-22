package ru.volgadev.papastory

import android.app.Application
import ru.volgadev.common.log.AndroidLoggerDelegate
import ru.volgadev.common.log.Logger
import ru.volgadev.papastory.di.ApplicationComponent
import ru.volgadev.papastory.di.DaggerApplicationComponent

class KidsCardApplication : Application() {

    private val logger = Logger.get("PapaStoryApplication")

    init {
        Logger.setDelegates(AndroidLoggerDelegate())
    }

    lateinit var appComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()
        logger.debug("onCreate()")
        appComponent = DaggerApplicationComponent.factory().create(applicationContext)
    }
}