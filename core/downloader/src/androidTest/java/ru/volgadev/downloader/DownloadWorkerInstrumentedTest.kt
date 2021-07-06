package ru.volgadev.downloader

import android.Manifest
import android.os.Environment
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.volgadev.common.logger.Logger
import ru.volgadev.common.logger.TestLoggerDelegate
import java.io.File

@RunWith(AndroidJUnit4::class)
class DownloadWorkerInstrumentedTest {

    private var logger: Logger

    private val IMAGE_FILE_PATH = Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_PICTURES
    ).absolutePath + "/1.jpg"
    private val IMAGE_URL =
        "https://i.pinimg.com/236x/c0/b7/7f/c0b77faeb2cb3e67fd1b423c4535f6c3.jpg"

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

        val downloader = Downloader(appContext, GlobalScope)
        GlobalScope.launch(Dispatchers.Default) {
            val isSuccess = downloader.download(IMAGE_URL, IMAGE_FILE_PATH)
            assert(isSuccess)
            assert(File(IMAGE_FILE_PATH).exists())

            logger.debug("downloadTest() end")

        }
        logger.debug("downloadTest() end")
    }
}