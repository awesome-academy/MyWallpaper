package com.sun.mywallpaper.ui.editphoto

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.sun.mywallpaper.R
import com.sun.mywallpaper.adapter.FilterAdapter
import com.sun.mywallpaper.base.BaseFragment
import com.sun.mywallpaper.base.FragmentInteractionListener
import com.sun.mywallpaper.base.OnRecyclerItemClickListener
import com.sun.mywallpaper.data.model.Photo
import com.sun.mywallpaper.databinding.FragmentPhotoEditorBinding
import com.sun.mywallpaper.di.KoinNames
import com.sun.mywallpaper.util.Constants
import com.sun.mywallpaper.util.StringUtils
import com.sun.mywallpaper.util.Utils
import com.sun.mywallpaper.util.showToast
import com.sun.mywallpaper.viewmodel.PhotoViewModel
import ja.burhanrashid52.photoeditor.*
import kotlinx.android.synthetic.main.fragment_photo_editor.*
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named
import java.io.File

class PhotoEditorFragment : BaseFragment<FragmentPhotoEditorBinding, PhotoViewModel>(),
    View.OnClickListener,
    OnPhotoEditorListener,
    BrushBottomDialogFragment.OnBrushBottomDialogFragmentInteractionListener,
    EmojiBottomDialogFragment.OnEmojiFragmentInteractionListener,
    OnRecyclerItemClickListener<Pair<String, PhotoFilter>> {

    override val layoutResource: Int
        get() = R.layout.fragment_photo_editor
    override val viewModel: PhotoViewModel by viewModel()

    private val filterAdapter: FilterAdapter = get(named(KoinNames.FILTER_ADAPTER))
    private val photo by lazy {
        arguments?.getParcelable<Photo>(PHOTO_EDITOR)
    }
    private val brushBottomDialogFragment = BrushBottomDialogFragment.newInstance()
    private val emojiBottomDialogFragment = EmojiBottomDialogFragment.newInstance()
    private var listener: OnPhotoEditorFragmentInteractionListener? = null
    private val photoEditor by lazy {
        activity?.let {
            PhotoEditor.Builder(context, photoEditorView)
                .setPinchTextScalable(true)
                .setDefaultTextTypeface(Typeface.createFromAsset(it.assets, Constants.FONT_FILE))
                .setDefaultEmojiTypeface(Typeface.createFromAsset(it.assets, Constants.EMOJI_FILE))
                .build()
        }
    }
    private var isFilterVisible = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnPhotoEditorFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context $ERROR_IMPLEMENT_FRAGMENT_INTERACTION_LISTENER")
        }
    }

    override fun initComponents() {
        initRecyclerView()
        initOther()
    }

    override fun onEditTextChangeListener(rootView: View, text: String?, colorCode: Int) {
        AddTextDialogFragment.show(childFragmentManager, text, colorCode, true)
            .setFragmentInteractionListener(object :
                AddTextDialogFragment.OnAddTextFragmentInteractionListener {
                override fun onDone(inputText: String, colorCode: Int) {
                    val styleBuilder = TextStyleBuilder()
                    styleBuilder.withTextColor(colorCode)
                    photoEditor?.editText(rootView, inputText, styleBuilder)
                    textCurrentTool.text = getString(R.string.edit_tool_add_text)
                }
            })
    }

    override fun onStartViewChangeListener(viewType: ViewType?) {
    }

    override fun onRemoveViewListener(viewType: ViewType?, numberOfAddedViews: Int) {
    }

    override fun onAddViewListener(viewType: ViewType?, numberOfAddedViews: Int) {
    }

    override fun onStopViewChangeListener(viewType: ViewType?) {
    }

    override fun onColorChanged(colorCode: Int) {
        photoEditor?.brushColor = colorCode
        textCurrentTool.text = getString(R.string.edit_tool_brush)
    }

    override fun onOpacityChanged(opacity: Int) {
        photoEditor?.setOpacity(opacity)
        textCurrentTool.text = getString(R.string.edit_tool_brush)
    }

    override fun onBrushSizeChanged(brushSize: Int) {
        photoEditor?.brushSize = brushSize.toFloat()
        textCurrentTool.text = getString(R.string.edit_tool_brush)
    }

    override fun emojiSelected(emojiUnicode: String) {
        photoEditor?.addEmoji(emojiUnicode)
        textCurrentTool.text = getString(R.string.edit_tool_emoji)
    }

    override fun showItemDetail(item: Pair<String, PhotoFilter>) {
        photoEditor?.setFilterEffect(item.second)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.buttonUndo -> photoEditor?.undo()

            R.id.buttonRedo -> photoEditor?.redo()

            R.id.buttonGallery -> {
            }

            R.id.buttonCamera -> {
            }

            R.id.buttonDownload -> {
                showSaveDialog()
            }

            R.id.buttonBrush -> {
                photoEditor?.setBrushDrawingMode(true)
                textCurrentTool.text = getString(R.string.edit_tool_brush)
                brushBottomDialogFragment.show(childFragmentManager, brushBottomDialogFragment.tag)
            }

            R.id.buttonAddText -> AddTextDialogFragment.show(
                childFragmentManager,
                Constants.EMPTY_STRING,
                Utils.getColorPicker(resources, R.color.white_color_picker),
                false
            ).setFragmentInteractionListener(object :
                AddTextDialogFragment.OnAddTextFragmentInteractionListener {
                override fun onDone(inputText: String, colorCode: Int) {
                    val styleBuilder = TextStyleBuilder()
                    styleBuilder.withTextColor(colorCode)
                    photoEditor?.addText(inputText, styleBuilder)
                    textCurrentTool.text = getString(R.string.edit_tool_add_text)
                }
            })

            R.id.buttonEraser -> {
                photoEditor?.brushEraser()
                textCurrentTool.text = getString(R.string.edit_tool_eraser)
            }

            R.id.buttonFilter -> {
                if (isFilterVisible) {
                    recyclerViewFilter.visibility = View.GONE
                    isFilterVisible = false
                } else {
                    recyclerViewFilter.visibility = View.VISIBLE
                    isFilterVisible = true
                }
                textCurrentTool.text = getString(R.string.edit_tool_filter)
            }

            R.id.buttonEmoji -> emojiBottomDialogFragment.show(
                childFragmentManager,
                emojiBottomDialogFragment.tag
            )
        }
    }

    override fun onBackPressed() = getNavigationManager().navigateBack()

    private fun initRecyclerView() {
        recyclerViewFilter.apply {
            adapter = filterAdapter.also {
                it.updateData(Utils.getFilterList())
                it.setOnRecyclerItemClickListener(this@PhotoEditorFragment)
            }
            setHasFixedSize(true)
        }
    }

    private fun initOther() {
        buttonUndo.setOnClickListener(this)
        buttonRedo.setOnClickListener(this)
        buttonGallery.setOnClickListener(this)
        buttonCamera.setOnClickListener(this)
        buttonDownload.setOnClickListener(this)
        buttonBrush.setOnClickListener(this)
        buttonAddText.setOnClickListener(this)
        buttonEraser.setOnClickListener(this)
        buttonFilter.setOnClickListener(this)
        buttonEmoji.setOnClickListener(this)

        brushBottomDialogFragment.setFragmentInteractionListener(this)
        emojiBottomDialogFragment.setFragmentInteractionListener(this)

        photoEditor?.setOnPhotoEditorListener(this)

        Glide.with(this)
            .load(photo?.urls?.regular)
            .into(photoEditorView.source)
    }

    private fun showSaveDialog() {
        context?.let {
            AlertDialog.Builder(it).apply {
                setMessage(getString(R.string.save_message))
                setPositiveButton(getString(R.string.save)) { _, _ ->
                    saveImage(false)
                }
                setNegativeButton(getString(R.string.set_as_wallpaper)) { _, _ ->
                    saveImage(true)
                }
                create()
                show()
            }
        }
    }

    private fun saveImage(isSetAsWallpaper: Boolean) {
        try {
            val filename = photo?.id + System.currentTimeMillis() + Constants.DOWNLOAD_PHOTO_FORMAT
            val file = File(StringUtils.getFilePath(filename))
            context?.showToast(getString(R.string.download_started_message))
            file.createNewFile()

            val saveSettings = SaveSettings.Builder()
                .setClearViewsEnabled(true)
                .setTransparencyEnabled(true)
                .build()

            photoEditor?.saveAsFile(
                file.absolutePath,
                saveSettings,
                object : PhotoEditor.OnSaveListener {

                    override fun onSuccess(imagePath: String) {
                        photoEditorView.source.setImageURI(Uri.fromFile(File(imagePath)))
                        if (isSetAsWallpaper)
                            setAsWallpaper(file)
                        else
                            context?.showToast(getString(R.string.download_finished_message))
                        onBackPressed()
                    }

                    override fun onFailure(exception: Exception) {
                        context?.showToast(exception.message.toString())
                    }
                })
        } catch (e: Exception) {
            context?.showToast(e.message.toString())
        }
    }

    private fun setAsWallpaper(file: File) {
        context?.let {
            val uri = StringUtils.getFileURi(it, file)
            startActivity(Utils.wallpaperIntent(it, uri))
        }
    }

    interface OnPhotoEditorFragmentInteractionListener : FragmentInteractionListener

    companion object {
        private const val ERROR_IMPLEMENT_FRAGMENT_INTERACTION_LISTENER =
            "must implement OnPhotoEditorFragmentInteractionListener"
        private const val PHOTO_EDITOR = "photo_editor"

        @JvmStatic
        fun newInstance(photo: Photo) = PhotoEditorFragment().apply {
            arguments = Bundle().apply {
                putParcelable(PHOTO_EDITOR, photo)
            }
        }
    }
}
