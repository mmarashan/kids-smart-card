package ru.volgadev.downloader

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.lifecycle.Observer
import androidx.work.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.volgadev.common.log.Logger
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

enum class DownloadResult {
    SUCCESS, FILE_SYSTEM_ERROR, CONNECTION_ERROR
}

// TODO: make it!
class Downloader(private val context: Context, private val scope: CoroutineScope) {

    private val logger = Logger.get("Downloader")

    @WorkerThread
    suspend fun download(url: String, filePath: String): DownloadResult {

        logger.debug("download() filePath=$filePath")

        val constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresStorageNotLow(true)
            .build()
        val oneTimeWorkRequest =
            OneTimeWorkRequest.Builder(DownloadWorker::class.java)
                .setInputData(
                    Data.Builder()
                        .putString(FILE_PATH_KEY, filePath)
                        .putString(URL_KEY, url)
                        .build()
                )
                .setConstraints(constraints).build()

        logger.debug("Start download")
        val workManager = WorkManager.getInstance(context)
        workManager.enqueue(oneTimeWorkRequest)
        var result: DownloadResult = DownloadResult.CONNECTION_ERROR
        val latch = CountDownLatch(1)
        val liveData = workManager
            .getWorkInfoByIdLiveData(oneTimeWorkRequest.id)

        var observer = Observer<WorkInfo> { logger.warn("Empty observer") }

        scope.launch(Dispatchers.Main) {
            observer =  Observer { workInfo: WorkInfo? ->
                if (workInfo != null) {
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                        result = DownloadResult.SUCCESS
                        liveData.removeObserver(observer)
                        latch.countDown()
                    }
                    if (workInfo.state == WorkInfo.State.FAILED) {
                        result = DownloadResult.CONNECTION_ERROR
                        liveData.removeObserver(observer)
                        latch.countDown()
                    }

                    val value = workInfo.progress.getInt(PROGRESS_KEY, 0)
                    logger.debug("Download progress: $value")
                }
            }
            liveData.observeForever(observer)
        }
        // TODO: remove it!
        latch.await(100, TimeUnit.SECONDS)
        logger.debug("result=${result.name}")
        return result
    }
}