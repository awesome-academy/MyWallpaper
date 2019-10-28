package com.sun.mywallpaper.base

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.sun.mywallpaper.util.Constants
import com.sun.mywallpaper.util.showToast


abstract class BaseFragment<VB : ViewDataBinding, VM : BaseViewModel> : Fragment(), LifecycleOwner {

    protected abstract val layoutResource: Int
    protected abstract val viewModel: VM
    protected lateinit var viewDataBinding: VB
    protected var loadMore = true
    private lateinit var navigationManagerInner: NavigationManager
    private lateinit var fragmentInteractionInner: FragmentInteractionListener

    override fun onAttach(context: Context) {
        super.onAttach(context)

        navigationManagerInner =
            if (parentFragment != null && parentFragment is HasNavigationManager) {
                (parentFragment as HasNavigationManager).provideNavigationManager()
            } else if (context is HasNavigationManager) {
                (context as HasNavigationManager).provideNavigationManager()
            } else {
                throw RuntimeException(ERROR_IMPLEMENT_HAS_NAVIGATION_MANAGER)
            }

        if (context is Activity) {
            fragmentInteractionInner = context as FragmentInteractionListener
        } else {
            throw RuntimeException(ERROR_IMPLEMENT_FRAGMENT_INTERACTION_LISTENER)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        viewDataBinding = DataBindingUtil.inflate(inflater, layoutResource, container, false) as VB
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initComponents()
        setBindingVariables()
        initData()
        observeData()
    }

    override fun onStart() {
        super.onStart()
        if (::fragmentInteractionInner.isInitialized) {
            fragmentInteractionInner.setCurrentFragment(this)
        }
    }

    protected abstract fun initComponents()

    protected open fun initData() {
        viewModel.create()
    }

    protected open fun observeData() {
        viewModel.messageNotification.observe(this, Observer {
            if (it == Constants.MESSAGE_DATA_NOT_FOUND)
                loadMore = false
            context?.showToast(it)
        })
    }

    open fun setBindingVariables() {
    }

    open fun onBackPressed() = false

    fun getNavigationManager(): NavigationManager = navigationManagerInner

    companion object {
        private const val ERROR_IMPLEMENT_HAS_NAVIGATION_MANAGER =
            "Activity host must implement HasNavigationManager"
        private const val ERROR_IMPLEMENT_FRAGMENT_INTERACTION_LISTENER =
            "Activity host must implement FragmentInteractionListener"
    }
}
