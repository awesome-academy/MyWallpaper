package com.sun.mywallpaper.ui.userdetail

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.sun.mywallpaper.R
import com.sun.mywallpaper.adapter.PhotoAdapter
import com.sun.mywallpaper.base.BaseFragment
import com.sun.mywallpaper.base.LastItemListener
import com.sun.mywallpaper.base.OnRecyclerItemClickListener
import com.sun.mywallpaper.data.model.Photo
import com.sun.mywallpaper.data.model.User
import com.sun.mywallpaper.databinding.FragmentPhotoBinding
import com.sun.mywallpaper.di.KoinNames
import com.sun.mywallpaper.util.Constants
import com.sun.mywallpaper.viewmodel.PhotoViewModel
import kotlinx.android.synthetic.main.fragment_photo.*
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named

class UserPhotoFragment : BaseFragment<FragmentPhotoBinding, PhotoViewModel>(),
    OnRecyclerItemClickListener<Photo> {

    override val layoutResource: Int
        get() = R.layout.fragment_photo
    override val viewModel: PhotoViewModel by viewModel()

    private val userPhotoAdapter: PhotoAdapter = get(named(KoinNames.USER_DETAIL_PHOTO_ADAPTER))
    private var page = Constants.DEFAULT_PAGE
    private val user by lazy {
        arguments?.getParcelable<User>(USER_DETAIL)
    }

    override fun initComponents() {
        recyclerViewPhoto.apply {
            layoutManager = LinearLayoutManager(context)
            adapter =
                userPhotoAdapter.also { it.setOnRecyclerItemClickListener(this@UserPhotoFragment) }
            hasFixedSize()
            addOnScrollListener(object : LastItemListener() {
                override fun onLastItemVisible() {
                    user?.let {
                        viewModel.getUserPhotos(
                            it.username,
                            ++page,
                            Constants.DEFAULT_PER_PAGE,
                            SORT_BY_LATEST
                        )
                    }
                }
            })
        }
        photoSwipeRefreshLayout.apply {
            user?.let {
                setOnRefreshListener {
                    page = 1
                    viewModel.refreshUserPhotos(
                        it.username,
                        page,
                        Constants.DEFAULT_PER_PAGE,
                        SORT_BY_LATEST
                    )
                    isRefreshing = false
                }
            }
        }
    }

    override fun setBindingVariables() {
        super.setBindingVariables()
        viewDataBinding.viewModel = this.viewModel
    }

    override fun initData() {
        super.initData()
        user?.let {
            viewModel.refreshUserPhotos(
                it.username,
                page,
                Constants.DEFAULT_PER_PAGE,
                SORT_BY_LATEST
            )
        }
    }

    override fun observeData() {
        super.observeData()
        viewModel.userPhotos.observe(viewLifecycleOwner, Observer {
            it?.let {
                userPhotoAdapter.updateData(it)
                progressBar.visibility = View.GONE
                recyclerViewPhoto.visibility = View.VISIBLE
            }
        })
    }

    override fun showItemDetail(item: Photo) {
    }

    companion object {
        private const val USER_DETAIL = "user_detail"
        private const val SORT_BY_LATEST = "latest"
        private const val SORT_BY_OLDEST = "oldest"
        private const val SORT_BY_POPULAR = "popular"

        @JvmStatic
        fun newInstance(user: User) = UserPhotoFragment().apply {
            arguments = Bundle().apply {
                putParcelable(USER_DETAIL, user)
            }
        }
    }
}
