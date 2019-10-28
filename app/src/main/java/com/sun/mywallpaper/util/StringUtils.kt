package com.sun.mywallpaper.util

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import com.sun.mywallpaper.BuildConfig
import java.io.File

object StringUtils {

    @Suppress("DEPRECATION")
    fun getFilePath(fileName: String?) =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            Environment.getExternalStorageDirectory().absolutePath + Constants.DOWNLOAD_PATH + fileName
        else
            Constants.EMPTY_STRING

    fun getFileURi(context: Context, file: File): Uri = FileProvider.getUriForFile(
        context,
        BuildConfig.APPLICATION_ID + Constants.AUTHORITY_PROVIDER,
        file
    )

    @Suppress("DEPRECATION")
    fun getFileURi(context: Context, fileName: String): Uri = FileProvider.getUriForFile(
        context,
        BuildConfig.APPLICATION_ID + Constants.AUTHORITY_PROVIDER,
        File(getFilePath(fileName))
    )
}
