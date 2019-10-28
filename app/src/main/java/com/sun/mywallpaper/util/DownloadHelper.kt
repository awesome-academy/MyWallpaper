package com.sun.mywallpaper.util

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import androidx.collection.ArrayMap
import java.io.File

class DownloadHelper(context: Context) {

    private val downloadManager by lazy {
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }
    private val downloads = ArrayMap<Long, String>()

    fun addDownloadRequest(
        downloadUrl: String,
        fileName: String,
        downloadType: DownloadType
    ): Long {

        val request = DownloadManager.Request(Uri.parse(downloadUrl))
            .setTitle(fileName)
            .setDestinationInExternalPublicDir(Constants.DOWNLOAD_PATH, fileName)
            .setNotificationVisibility(
                if (downloadType == DownloadType.DOWNLOAD)
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                else
                    DownloadManager.Request.VISIBILITY_VISIBLE
            )

        val downloadId = downloadManager.enqueue(request)
        downloads[downloadId] = fileName

        return downloadId
    }

    fun removeDownloadRequest(id: Long) {
        downloadManager.remove(id)
        downloads.remove(id)
    }

    fun getDownloadCursor(id: Long): Cursor? {
        val cursor = downloadManager.query(DownloadManager.Query().setFilterById(id))
        return if (cursor == null) {
            null
        } else if (cursor.count > 0 && cursor.moveToFirst()) {
            cursor
        } else {
            cursor.close()
            null
        }
    }

    fun getDownloadStatus(cursor: Cursor) =
        when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
            DownloadManager.STATUS_SUCCESSFUL -> DownloadStatus.SUCCESS

            DownloadManager.STATUS_FAILED, DownloadManager.STATUS_PAUSED ->
                DownloadStatus.FAILED

            else -> DownloadStatus.DOWNLOADING
        }

    fun fileExists(fileName: String) = File(StringUtils.getFilePath(fileName)).exists()

    fun getFilePath(id: Long) = StringUtils.getFilePath(getFileName(id))

    private fun getFileName(id: Long) = downloads[id]
}
