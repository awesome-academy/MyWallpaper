package com.sun.mywallpaper.util

import android.Manifest
import android.app.Activity
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import com.sun.mywallpaper.R
import com.sun.mywallpaper.data.api.ApiConstants
import ja.burhanrashid52.photoeditor.PhotoFilter
import java.io.IOException

object Utils {

    fun isStoragePermissionGranted(activity: Activity) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(activity)) {
                true
            } else {
                requestPermissions(activity)
                false
            }
        } else {
            true
        }

    fun getColorPicker(resources: Resources, colorCode: Int) =
        ResourcesCompat.getColor(resources, colorCode, null)

    fun getColorPickerList(resources: Resources): List<Int> = mutableListOf<Int>().apply {
        add(getColorPicker(resources, R.color.blue_color_picker))
        add(getColorPicker(resources, R.color.brown_color_picker))
        add(getColorPicker(resources, R.color.green_color_picker))
        add(getColorPicker(resources, R.color.orange_color_picker))
        add(getColorPicker(resources, R.color.red_color_picker))
        add(getColorPicker(resources, R.color.black_color_picker))
        add(getColorPicker(resources, R.color.red_orange_color_picker))
        add(getColorPicker(resources, R.color.sky_blue_color_picker))
        add(getColorPicker(resources, R.color.violet_color_picker))
        add(getColorPicker(resources, R.color.white_color_picker))
        add(getColorPicker(resources, R.color.yellow_color_picker))
        add(getColorPicker(resources, R.color.yellow_green_color_picker))
    }

    fun getFilterList(): List<Pair<String, PhotoFilter>> =
        mutableListOf<Pair<String, PhotoFilter>>().apply {
            add(Pair(Constants.FILTER_NONE, PhotoFilter.NONE))
            add(Pair(Constants.FILTER_AUTO_FIX, PhotoFilter.AUTO_FIX))
            add(Pair(Constants.FILTER_BRIGHTNESS, PhotoFilter.BRIGHTNESS))
            add(Pair(Constants.FILTER_CONTRAST, PhotoFilter.CONTRAST))
            add(Pair(Constants.FILTER_DOCUMENTARY, PhotoFilter.DOCUMENTARY))
            add(Pair(Constants.FILTER_DUE_TONE, PhotoFilter.DUE_TONE))
            add(Pair(Constants.FILTER_FILL_LIGHT, PhotoFilter.FILL_LIGHT))
            add(Pair(Constants.FILTER_FISH_EYE, PhotoFilter.FISH_EYE))
            add(Pair(Constants.FILTER_GRAIN, PhotoFilter.GRAIN))
            add(Pair(Constants.FILTER_GRAY_SCAL, PhotoFilter.GRAY_SCALE))
            add(Pair(Constants.FILTER_LOMISH, PhotoFilter.LOMISH))
            add(Pair(Constants.FILTER_NEGATIVE, PhotoFilter.NEGATIVE))
            add(Pair(Constants.FILTER_POSTERIZE, PhotoFilter.POSTERIZE))
            add(Pair(Constants.FILTER_SATURATE, PhotoFilter.SATURATE))
            add(Pair(Constants.FILTER_SEPIA, PhotoFilter.SEPIA))
            add(Pair(Constants.FILTER_SHARPEN, PhotoFilter.SHARPEN))
            add(Pair(Constants.FILTER_TEMPERATURE, PhotoFilter.TEMPERATURE))
            add(Pair(Constants.FILTER_TINT, PhotoFilter.TINT))
            add(Pair(Constants.FILTER_VIGNETTE, PhotoFilter.VIGNETTE))
            add(Pair(Constants.FILTER_CROSS_PROCESS, PhotoFilter.CROSS_PROCESS))
            add(Pair(Constants.FILTER_FLIP_HORIZONTAL, PhotoFilter.FLIP_HORIZONTAL))
            add(Pair(Constants.FILTER_FLIP_VERTICAL, PhotoFilter.FLIP_VERTICAL))
            add(Pair(Constants.FILTER_ROTATE, PhotoFilter.ROTATE))
        }

    fun getBitmapFromAsset(context: Context, name: String) = try {
        val inputStream = context.assets.open(name)
        BitmapFactory.decodeStream(inputStream)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }

    fun viewIntent(url: String?) = Intent(
        Intent.ACTION_VIEW,
        Uri.parse(url + ApiConstants.UNSPLASH_UTM_PARAMETERS)
    )

    fun shareIntent(url: String?) = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, url + ApiConstants.UNSPLASH_UTM_PARAMETERS)
    }

    fun wallpaperIntent(context: Context, uri: Uri): Intent =
        WallpaperManager.getInstance(context).getCropAndSetWallpaperIntent(uri).apply {
            setDataAndType(uri, "image/*")
            putExtra("mimeType", "image/*")
        }

    private fun checkSelfPermission(activity: Activity) = ActivityCompat.checkSelfPermission(
        activity,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestPermissions(activity: Activity) = ActivityCompat.requestPermissions(
        activity,
        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
        1
    )
}
