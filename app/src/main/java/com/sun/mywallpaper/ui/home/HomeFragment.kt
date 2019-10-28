package com.sun.mywallpaper.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.sun.mywallpaper.R
import com.sun.mywallpaper.adapter.PagerAdapter
import com.sun.mywallpaper.base.BaseFragment
import com.sun.mywallpaper.base.FragmentInteractionListener
import com.sun.mywallpaper.databinding.FragmentHomeBinding
import com.sun.mywallpaper.ui.search.SearchFragment
import com.sun.mywallpaper.util.Utils
import com.sun.mywallpaper.util.showToast
import com.sun.mywallpaper.viewmodel.HomeViewModel
import kotlinx.android.synthetic.main.fragment_home.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

@SuppressLint("StaticFieldLeak")
class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>(), View.OnClickListener {

    override val layoutResource: Int
        get() = R.layout.fragment_home
    override val viewModel: HomeViewModel by viewModel()

    private var listener: OnHomeFragmentInteractionListener? = null
    private lateinit var pagerAdapter: PagerAdapter
    private var clearCache: AsyncTask<Unit, Unit, Unit>? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnHomeFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context $ERROR_IMPLEMENT_FRAGMENT_INTERACTION_LISTENER")
        }
    }

    override fun onResume() {
        super.onResume()
        setCacheSizeLabel()
    }

    override fun onPause() {
        super.onPause()
        clearCache?.cancel(true)
    }

    override fun initComponents() {
        initToolbar()
        initViewPager()
        initFab()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.home, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.actionSearch -> {
                getNavigationManager().open(SearchFragment.newInstance())
                true
            }

            else -> super.onOptionsItemSelected(item)
        }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.fabSetting -> {
                fabMenu.close(true)
            }

            R.id.fabClearCache -> {
                fabMenu.close(true)
                context?.let {
                    clearCache = ClearCache(it).execute()
                    fabClearCache.labelText = getString(R.string.drawer_clear_cache, "0")
                }
            }
        }
    }

    private fun initToolbar() {
        (activity as AppCompatActivity).apply {
            setSupportActionBar(toolBarHome)
            supportActionBar?.setTitle(R.string.app_name)
            Utils.isStoragePermissionGranted(this)
        }
        setHasOptionsMenu(true)
    }

    private fun initViewPager() {
        pagerAdapter = PagerAdapter(childFragmentManager)
        pagerAdapter.apply {
            addFragment(NewFragment.newInstance(), getString(R.string.drawer_new))
            addFragment(FeaturedFragment.newInstance(), getString(R.string.drawer_featured))
            addFragment(CollectionFragment.newInstance(), getString(R.string.drawer_collections))
        }
        viewPager.apply {
            adapter = pagerAdapter
            offscreenPageLimit = 2
        }

        tabLayout.setupWithViewPager(viewPager)
    }

    private fun initFab() {
        fabMenu.setClosedOnTouchOutside(true)
        fabSetting.setOnClickListener(this)
        fabClearCache.setOnClickListener(this)
    }

    private fun setCacheSizeLabel() {
        context?.run {
            Glide.getPhotoCacheDir(this)?.let {
                fabClearCache.labelText =
                    getString(R.string.drawer_clear_cache, dirSize(it).toString())
            }
        }
    }

    private fun dirSize(dir: File): Long {
        if (dir.exists()) {
            var result: Long = 0
            val files = dir.listFiles()
            files?.let {
                for (item in it) {
                    result += if (item.isDirectory) {
                        dirSize(item)
                    } else {
                        item.length()
                    }
                }
                return result / (1024 * 1024)
            }
        }
        return 0
    }

    class ClearCache(private val context: Context) : AsyncTask<Unit, Unit, Unit>() {

        override fun doInBackground(vararg params: Unit?) {
            Glide.get(context).clearDiskCache()
        }

        override fun onPostExecute(result: Unit?) {
            context.showToast(context.getString(R.string.cache_clear_message))
        }
    }

    interface OnHomeFragmentInteractionListener : FragmentInteractionListener

    companion object {
        private const val ERROR_IMPLEMENT_FRAGMENT_INTERACTION_LISTENER =
            "must implement OnHomeFragmentInteractionListener"

        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}
