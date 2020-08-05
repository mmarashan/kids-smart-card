package ru.volgadev.downloader

import android.Manifest
import android.os.Environment
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.volgadev.common.log.Logger
import ru.volgadev.common.log.TestLoggerDelegate
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


@RunWith(AndroidJUnit4::class)
class DownloadWorkerInstrumentedTest {

    private var logger: Logger

    @Rule
    @JvmField
    var mRuntimePermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

    init {
        Logger.setDelegates(TestLoggerDelegate())
        logger = Logger.get("DownloadWorkerInstrumentedTest")
        logger.debug("init")
    }

    @Test
    fun downloadTest() {
        logger.debug("downloadTest()")
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val filePath = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        ).absolutePath + "/1.jpg"
        val url = "https://i.pinimg.com/236x/c0/b7/7f/c0b77faeb2cb3e67fd1b423c4535f6c3.jpg"

        val constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresStorageNotLow(true)
            .build()
        val oneTimeWorkRequest =
            OneTimeWorkRequest.Builder(DownloadWorker::class.java)
                .setInputData(
                    Data.Builder()
                        .putString(FILE_PATH_KEY, filePath)
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

        val latch = CountDownLatch(1)

        GlobalScope.launch(Dispatchers.Main) {
            workManager
                .getWorkInfoByIdLiveData(oneTimeWorkRequest.id)
                .observeForever { workInfo: WorkInfo? ->
                    if (workInfo != null) {
                        if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                            latch.countDown()
                            return@observeForever
                        }
                        if (workInfo.state == WorkInfo.State.FAILED) {
                            assert(false, { "Test not passed" })
                        }

                        val progress = workInfo.progress
                        val value = progress.getInt(PROGRESS_KEY, 0)
                        logger.debug("Download progress: $value")
                        if (value == 100) {
                            latch.countDown()
                        }
                    }
                }
        }
        latch.await(2, TimeUnit.SECONDS)

        assert(File(filePath).exists())

        logger.debug("downloadTest() end")
    }
}