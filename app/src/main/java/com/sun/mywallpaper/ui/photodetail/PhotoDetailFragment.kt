package com.sun.mywallpaper.ui.photodetail

import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.sun.mywallpaper.R
import com.sun.mywallpaper.base.BaseFragment
import com.sun.mywallpaper.base.FragmentInteractionListener
import com.sun.mywallpaper.data.model.Photo
import com.sun.mywallpaper.databinding.FragmentPhotoDetailBinding
import com.sun.mywallpaper.di.KoinNames
import com.sun.mywallpaper.ui.editphoto.PhotoEditorFragment
import com.sun.mywallpaper.ui.userdetail.UserDetailFragment
import com.sun.mywallpaper.util.*
import com.sun.mywallpaper.viewmodel.PhotoViewModel
import kotlinx.android.synthetic.main.fragment_photo_detail.*
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named
import java.io.File

class PhotoDetailFragment : BaseFragment<FragmentPhotoDetailBinding, PhotoViewModel>(),
    View.OnClickListener,
    WallpaperDialogFragment.OnWallpaperDialogFragmentInteractionListener {

    override val layoutResource: Int
        get() = R.layout.fragment_photo_detail
    override val viewModel: PhotoViewModel by viewModel()

    private val downloadHelper: DownloadHelper = get(named(KoinNames.DOWNLOAD_HELPER))
    private var listener: OnPhotoDetailFragmentInteractionListener? = null

    private val photo by lazy {
        arguments?.getParcelable<Photo>(PHOTO_DETAIL)
    }

    private var wallpaperDialogFragment: WallpaperDialogFragment? = null
    private lateinit var currentAction: DownloadType
    private var downloadReference: Long = 0
    private val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            val reference = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadReference == reference) {
                val cursor = downloadHelper.getDownloadCursor(downloadReference)
                cursor?.let {
                    if (downloadHelper.getDownloadStatus(it) == DownloadStatus.SUCCESS) {
                        setAsWallpaper()
                    }
                    it.close()
                }
                if (currentAction == DownloadType.WALLPAPER) {
                    wallpaperDialogFragment?.dismiss()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == 12345) {
            val uri = data?.data
            if (currentAction == DownloadType.WALLPAPER) {
                context?.let {
                    startActivity(Utils.wallpaperIntent(it, uri!!))
                }
                wallpaperDialogFragment?.setDownloadFinished(true)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnPhotoDetailFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context $ERROR_IMPLEMENT_FRAGMENT_INTERACTION_LISTENER")
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.registerReceiver(receiver, filter)
    }

    override fun onPause() {
        super.onPause()
        activity?.unregisterReceiver(receiver)
    }

    override fun initComponents() {
        initToolbar()
        initFab()
        initOther()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.detail, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }

            R.id.actionViewOnUnsplash -> {
                startActivity(Utils.viewIntent(photo?.links?.html))
                true
            }

            R.id.actionShare -> {
                startActivity(Utils.shareIntent(photo?.links?.html))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imagePhoto -> photo?.let {
                getNavigationManager().open(PhotoEditorFragment.newInstance(it))
            }

            R.id.imageUser -> photo?.let {
                getNavigationManager().open(UserDetailFragment.newInstance(it.user))
            }

            R.id.textUserName -> photo?.let {
                getNavigationManager().open(UserDetailFragment.newInstance(it.user))
            }

            R.id.fabDownload -> {
                fabMenu.close(true)
                currentAction = DownloadType.DOWNLOAD
                photo?.let { downloadImage(it.urls.regular, DownloadType.DOWNLOAD) }
            }

            R.id.fabWallpaper -> {
                fabMenu.close(true)
                currentAction = DownloadType.WALLPAPER
                photo?.let {
                    downloadImage(it.urls.regular, DownloadType.WALLPAPER)
                }
            }

            R.id.fabInfo -> {
                fabMenu.close(true)
            }

            R.id.fabStats -> {
                fabMenu.close(true)
            }
        }
    }

    override fun onBackPressed() = getNavigationManager().navigateBack()

    override fun onCancel() {
        downloadHelper.removeDownloadRequest(downloadReference)
    }

    private fun initToolbar() {
        (activity as AppCompatActivity).apply {
            setSupportActionBar(toolBar)
            supportActionBar?.title = Constants.EMPTY_STRING
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        setHasOptionsMenu(true)
    }

    private fun initFab() {
        fabMenu.setClosedOnTouchOutside(true)
        fabDownload.setOnClickListener(this)
        fabWallpaper.setOnClickListener(this)
        fabInfo.setOnClickListener(this)
        fabStats.setOnClickListener(this)
    }

    private fun initOther() {
        Glide.with(this)
            .load(photo?.urls?.regular)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imagePhoto)

        Glide.with(this)
            .load(photo?.user?.profileImage?.large)
            .apply(RequestOptions.circleCropTransform())
            .into(imageUser)

        textUserName.text = photo?.user?.name
        textPhotoStoryTitle.text =
            photo?.story?.title?.also { textPhotoStoryTitle.visibility = View.VISIBLE }
        textPhotoDescription.text =
            photo?.description?.also { textPhotoDescription.visibility = View.VISIBLE }
        textPhotoLocation.text = photo?.location?.title
        textPhotoCreatedAt.text = photo?.createdAt
        textPhotoLikes.text = getString(R.string.likes, photo?.likes.toString())
        textPhotoDownloads.text = getString(R.string.downloads, photo?.downloads.toString())
        textPhotoColor.text = photo?.color
        imagePhotoColor.setBackgroundColor(Color.parseColor(photo?.color))

        imagePhoto.setOnClickListener(this)
        imageUser.setOnClickListener(this)
        textUserName.setOnClickListener(this)
    }

    private fun downloadImage(url: String, downloadType: DownloadType) {
        val filename = photo?.id + Constants.DOWNLOAD_PHOTO_FORMAT

        if (downloadHelper.fileExists(filename)) {
            if (downloadType == DownloadType.WALLPAPER) {
                context?.let {
                    val uri = StringUtils.getFileURi(it, filename)
                    startActivity(Utils.wallpaperIntent(it, uri))
                }
            } else
                context?.showToast(getString(R.string.download_fail_message))
        } else {
            if (downloadType == DownloadType.WALLPAPER) {
                wallpaperDialogFragment = WallpaperDialogFragment()
                wallpaperDialogFragment?.setFragmentInteractionListener(this)
                wallpaperDialogFragment?.show(childFragmentManager, null)
            } else {
                context?.showToast(getString(R.string.download_started_message))
            }
            downloadReference =
                downloadHelper.addDownloadRequest(url, filename, downloadType)
        }
    }

    private fun setAsWallpaper() {
        val file = File(downloadHelper.getFilePath(downloadReference))
        context?.let {
            it.showToast(getString(R.string.download_finished_message))
            val uri = StringUtils.getFileURi(it, file)
            if (currentAction == DownloadType.WALLPAPER) {
                startActivity(Utils.wallpaperIntent(it, uri))
                wallpaperDialogFragment?.setDownloadFinished(true)
            }
        }
    }

    interface OnPhotoDetailFragmentInteractionListener : FragmentInteractionListener

    companion object {
        private const val ERROR_IMPLEMENT_FRAGMENT_INTERACTION_LISTENER =
            "must implement OnPhotoDetailFragmentInteractionListener"
        private const val PHOTO_DETAIL = "photo_detail"

        @JvmStatic
        fun newInstance(photo: Photo) = PhotoDetailFragment().apply {
            arguments = Bundle().apply {
                putParcelable(PHOTO_DETAIL, photo)
            }
        }
    }
}
