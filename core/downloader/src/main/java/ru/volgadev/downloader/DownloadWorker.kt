package ru.volgadev.downloader

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import ru.volgadev.common.log.Logger
import java.io.File
import java.io.FileOutputStream
import java.net.URL

const val FILE_PATH_KEY = "file_path"
const val URL_KEY = "url_str"
const val PROGRESS_KEY = "progress"

class DownloadWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val logger =
        Logger.get("DownloadWorker")

    override suspend fun doWork(): Result {
        try {
            val filePath = inputData.getString(FILE_PATH_KEY)
            val urlStr = inputData.getString(URL_KEY)

            if (filePath == null) {
                throw KotlinNullPointerException("$FILE_PATH_KEY is null")
            }

            if (urlStr == null) {
                throw KotlinNullPointerException("$URL_KEY is null")
            }

            val outputFile = File(filePath)
            val url = URL(urlStr)
            val urlConnection = url.openConnection()
            urlConnection.connect()
            val fileLength = urlConnection.contentLength
            val fos = FileOutputStream(outputFile)
            val inputStream = urlConnection.getInputStream()
            val buffer = ByteArray(1024)
            var len1: Int
            var total: Long = 0
            while (inputStream.read(buffer).also { len1 = it } > 0) {
                total += len1.toLong()
                logger.debug("Download total $total bytes")
                val percentage = (1f * total * 100 / fileLength).toInt()
                fos.write(buffer, 0, len1)
                val progress = Data.Builder().putInt(PROGRESS_KEY, percentage).build()
                setProgress(progress)
            }
            fos.close()
            inputStream.close()
        } catch (e: Exception) {
            logger.error(e.toString())
            e.printStackTrace()
            return Result.failure()
        }
        return Result.success()
    }
}