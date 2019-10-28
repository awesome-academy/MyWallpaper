package com.sun.mywallpaper

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.sun.mywallpaper.base.BaseFragment
import com.sun.mywallpaper.base.HasNavigationManager
import com.sun.mywallpaper.base.NavigationManager
import com.sun.mywallpaper.ui.collectiondetail.CollectionDetailFragment
import com.sun.mywallpaper.ui.editphoto.PhotoEditorFragment
import com.sun.mywallpaper.ui.home.HomeFragment
import com.sun.mywallpaper.ui.photodetail.PhotoDetailFragment
import com.sun.mywallpaper.ui.search.SearchFragment
import com.sun.mywallpaper.ui.userdetail.UserDetailFragment
import com.sun.mywallpaper.util.showToast

class MainActivity : AppCompatActivity(),
    HomeFragment.OnHomeFragmentInteractionListener,
    CollectionDetailFragment.OnCollectionDetailFragmentInteractionListener,
    UserDetailFragment.OnUserDetailFragmentInteractionListener,
    SearchFragment.OnSearchFragmentInteractionListener,
    PhotoDetailFragment.OnPhotoDetailFragmentInteractionListener,
    PhotoEditorFragment.OnPhotoEditorFragmentInteractionListener,
    HasNavigationManager {

    private lateinit var navigationManager: NavigationManager
    private lateinit var currentFragment: BaseFragment<*, *>

    override fun onCreate(savedInstanceState: Bundle?) {
        navigationManager = NavigationManager(supportFragmentManager, R.id.mainContainer)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        savedInstanceState ?: navigationManager.openAsRoot(HomeFragment.newInstance())
    }

    override fun onBackPressed() {
        if (currentFragment is PhotoEditorFragment)
            AlertDialog.Builder(this).apply {
                setMessage(getString(R.string.save_message))
                setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                    dialog.dismiss()
                }
                setNegativeButton(getString(R.string.no)) { _, _ ->
                    super.onBackPressed()
                }
                create()
                show()
            }
        else
            super.onBackPressed()
    }

    override fun setCurrentFragment(fragment: BaseFragment<*, *>) {
        currentFragment = fragment
    }

    override fun provideNavigationManager() = navigationManager
}
