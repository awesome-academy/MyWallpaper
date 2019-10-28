package com.sun.mywallpaper.ui.collectiondetail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.sun.mywallpaper.R
import com.sun.mywallpaper.adapter.PhotoAdapter
import com.sun.mywallpaper.base.BaseFragment
import com.sun.mywallpaper.base.FragmentInteractionListener
import com.sun.mywallpaper.base.LastItemListener
import com.sun.mywallpaper.base.OnRecyclerItemClickListener
import com.sun.mywallpaper.data.api.ApiConstants
import com.sun.mywallpaper.data.model.Collection
import com.sun.mywallpaper.data.model.Photo
import com.sun.mywallpaper.databinding.FragmentCollectionDetailBinding
import com.sun.mywallpaper.di.KoinNames
import com.sun.mywallpaper.ui.photodetail.PhotoDetailFragment
import com.sun.mywallpaper.ui.userdetail.UserDetailFragment
import com.sun.mywallpaper.util.Constants
import com.sun.mywallpaper.util.Utils
import com.sun.mywallpaper.viewmodel.PhotoViewModel
import kotlinx.android.synthetic.main.fragment_collection_detail.*
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named

class CollectionDetailFragment :
    BaseFragment<FragmentCollectionDetailBinding, PhotoViewModel>(),
    OnRecyclerItemClickListener<Photo> {

    override val layoutResource: Int
        get() = R.layout.fragment_collection_detail
    override val viewModel: PhotoViewModel by viewModel()

    private var listener: OnCollectionDetailFragmentInteractionListener? = null
    private val collectionPhotoAdapter: PhotoAdapter =
        get(named(KoinNames.COLLECTION_DETAIL_ADAPTER))
    private var page = Constants.DEFAULT_PAGE
    private val collection by lazy {
        arguments?.getParcelable<Collection>(COLLECTION_DETAIL)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnCollectionDetailFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context $ERROR_IMPLEMENT_FRAGMENT_INTERACTION_LISTENER")
        }
    }

    override fun initComponents() {
        initToolbar()
        initRecyclerView()
        initSwipeRefreshLayout()
        initOther()
    }

    override fun setBindingVariables() {
        super.setBindingVariables()
        viewDataBinding.viewModel = this.viewModel
    }

    override fun initData() {
        super.initData()
        refreshCollectionPhotos()
    }

    override fun observeData() {
        super.observeData()
        viewModel.collectionPhotos.observe(viewLifecycleOwner, Observer {
            it?.let {
                collectionPhotoAdapter.updateData(it)
                progressBar.visibility = View.GONE
                recyclerViewCollectionDetail.visibility = View.VISIBLE
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.detail, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }

            R.id.actionViewOnUnsplash -> {
                startActivity(Utils.viewIntent(collection?.links?.html))
                true
            }

            R.id.actionShare -> {
                startActivity(Utils.shareIntent(collection?.links?.html))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }

    override fun onBackPressed() = getNavigationManager().navigateBack()

    override fun showItemDetail(item: Photo) {
        getNavigationManager().open(PhotoDetailFragment.newInstance(item))
    }

    private fun initToolbar() {
        (activity as AppCompatActivity).apply {
            setSupportActionBar(toolBarCollectionDetail)
            supportActionBar?.title = collection?.title
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        setHasOptionsMenu(true)
    }

    private fun initRecyclerView() {
        recyclerViewCollectionDetail.apply {
            layoutManager = LinearLayoutManager(context)
            adapter =
                collectionPhotoAdapter.also { it.setOnRecyclerItemClickListener(this@CollectionDetailFragment) }
            hasFixedSize()
            addOnScrollListener(object : LastItemListener() {
                override fun onLastItemVisible() {
                    if (loadMore)
                        getCollectionPhotos()
                }
            })
        }
    }

    private fun initSwipeRefreshLayout() {
        collectionDetailSwipeRefreshLayout.apply {
            setOnRefreshListener {
                refreshCollectionPhotos()
                isRefreshing = false
            }
        }
    }

    private fun refreshCollectionPhotos() {
        page = Constants.DEFAULT_PAGE
        loadMore = true
        collection?.let {
            viewModel.refreshCollectionPhotos(it.id, ++page, DEFAULT_PER_PAGE)
        }
    }

    private fun getCollectionPhotos() {
        collection?.let {
            viewModel.getCollectionPhotos(it.id, ++page, DEFAULT_PER_PAGE)
        }
    }

    private fun initOther() {
        collection?.let {
            Glide.with(this)
                .load(it.user.profileImage.medium)
                .apply(RequestOptions.circleCropTransform())
                .into(imageProfileCollection)
            textCollectionDescription.text = it.description
            textUserCollection.text = it.user.name

            imageProfileCollection.setOnClickListener { _ ->
                getNavigationManager().open(UserDetailFragment.newInstance(it.user))
            }
            textUserCollection.setOnClickListener { _ ->
                getNavigationManager().open(UserDetailFragment.newInstance(it.user))
            }
        }
    }

    interface OnCollectionDetailFragmentInteractionListener : FragmentInteractionListener

    companion object {
        private const val ERROR_IMPLEMENT_FRAGMENT_INTERACTION_LISTENER =
            "must implement OnHomeFragmentInteractionListener"
        private const val COLLECTION_DETAIL = "collection_detail"
        private const val DEFAULT_PER_PAGE = 30

        @JvmStatic
        fun newInstance(collection: Collection) = CollectionDetailFragment().apply {
            arguments = Bundle().apply {
                putParcelable(COLLECTION_DETAIL, collection)
            }
        }
    }
}
