package ru.volgadev.downloader

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.volgadev.common.logger.Logger

class Downloader(context: Context, private val ioDispatcher: CoroutineDispatcher) {

    private val logger = Logger.get("Downloader")
    private val workManager = WorkManager.getInstance(context)

    suspend fun download(url: String, filePath: String): Boolean = withContext(ioDispatcher) {

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
        workManager.enqueue(oneTimeWorkRequest)
        var hasResult = false
        var result = false
        var lastProgress = -1
        while (!hasResult) {
            val future = workManager.getWorkInfoById(oneTimeWorkRequest.id)
            val workInfo = future.await()
            if (workInfo.state == WorkInfo.State.RUNNING) {
                val progress = workInfo.progress.getInt(PROGRESS_KEY, 0)
                if (lastProgress != progress) {
                    lastProgress = progress
                    logger.debug("Load progress $progress")
                }
            }
            hasResult = (workInfo.state == WorkInfo.State.SUCCEEDED)
                    || (workInfo.state == WorkInfo.State.FAILED)
            result = workInfo.state == WorkInfo.State.SUCCEEDED
        }
        logger.debug("Download result $result")
        return@withContext result
    }
}