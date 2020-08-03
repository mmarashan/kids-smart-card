package ru.volgadev.downloader

import androidx.lifecycle.Observer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import org.junit.Test
import org.junit.runner.RunWith
import ru.volgadev.common.log.Logger
import ru.volgadev.common.log.TestLoggerDelegate
import java.io.File


/* TODO this test*/
@RunWith(AndroidJUnit4::class)
class DownloaderInstrumentedTest {

    private val logger =
        Logger.get(TestLoggerDelegate("DownloaderInstrumentedTest"))

//    @Test
//    fun useAppContext() {
//        // Context of the app under test.
//        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
//        logger.debug("Package name ${appContext.packageName}")
//        assertEquals("ru.volgadev.downloader", appContext.packageName)
//    }

    @Test
    fun download() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val filePath = "/data/data/1.jpg"
        val url = "https://i.pinimg.com/236x/c0/b7/7f/c0b77faeb2cb3e67fd1b423c4535f6c3.jpg"

        val constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresStorageNotLow(true)
            .build()
        val oneTimeWorkRequest =
            OneTimeWorkRequest.Builder(DownloadWorker::class.java)
                .setInputData(
                    Data.Builder()
                        .putString(FILE_PATH_KEY, "/data/data/1.jpg")
                        .putString(
                            URL_KEY,
                            url
                        )
                        .build()
                )
                .setConstraints(constraints).build()

        logger.debug("Start download")
        val workManager = WorkManager.getInstance(appContext)
        workManager.enqueue(oneTimeWorkRequest)

        Dispatchers.Main.run {
            workManager
                .getWorkInfoByIdLiveData(oneTimeWorkRequest.id)
                .observeForever(
                    Observer { workInfo: WorkInfo? ->
                        if (workInfo != null) {
                            val progress = workInfo.progress
                            val value = progress.getInt(PROGRESS_KEY, 0)
                            logger.debug("Download progress: $value")
                            if (value == 100) {
                                val file = File(filePath)
                                assert(file.exists())
                            }
                        }
                    }
                )
        }
    }
}