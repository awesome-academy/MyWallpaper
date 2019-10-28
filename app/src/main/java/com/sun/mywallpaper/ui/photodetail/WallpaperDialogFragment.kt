package com.sun.mywallpaper.ui.photodetail

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.sun.mywallpaper.R

class WallpaperDialogFragment : DialogFragment() {

    interface OnWallpaperDialogFragmentInteractionListener {
        fun onCancel()
    }

    private var listener: OnWallpaperDialogFragmentInteractionListener? = null
    private var downloadFinished = false

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(activity).inflate(
            R.layout.fragment_wallpaper_dialog,
            null,
            false
        )

        return AlertDialog.Builder(activity)
            .setTitle(R.string.setting_wallpaper)
            .setNegativeButton(R.string.cancel, null)
            .setCancelable(false)
            .setView(view)
            .create()
    }

    fun setFragmentInteractionListener(listener: OnWallpaperDialogFragmentInteractionListener) {
        this.listener = listener
    }

    fun setDownloadFinished(downloadFinished: Boolean) {
        this.downloadFinished = downloadFinished
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (!downloadFinished) {
            listener?.onCancel()
        }
        downloadFinished = false
    }
}
