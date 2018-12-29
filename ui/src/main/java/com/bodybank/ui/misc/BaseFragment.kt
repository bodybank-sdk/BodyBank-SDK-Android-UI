package com.bodybank.ui.misc

import android.content.Intent
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.util.SparseIntArray

open class BaseFragment : Fragment() {
    private val mRequestCodes = SparseIntArray()

    /**
     * Registers request code (used in
     * [.startActivityForResult]).
     *
     * @param requestCode the request code.
     * @param id          the fragment ID (can be [Fragment.getId] of
     * [Fragment.hashCode]).
     */
    open fun registerRequestCode(requestCode: Int, id: Int) {
        mRequestCodes.put(requestCode, id)
    }// registerRequestCode()

    open override fun startActivityForResult(intent: Intent, requestCode: Int) {
        if (parentFragment is BaseFragment) {
            (parentFragment as BaseFragment).registerRequestCode(
                requestCode, hashCode()
            )
            parentFragment!!.startActivityForResult(intent, requestCode)
        } else
            super.startActivityForResult(intent, requestCode)
    }// startActivityForResult()

    open override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!checkNestedFragmentsForResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data)
    }// onActivityResult()

    /**
     * Checks to see whether there is any children fragments which has been
     * registered with `requestCode` before. If so, let it handle the
     * `requestCode`.
     *
     * @param requestCode the code from [.onActivityResult].
     * @param resultCode  the code from [.onActivityResult].
     * @param data        the data from [.onActivityResult].
     * @return `true` if the results have been handed over to some child
     * fragment. `false` otherwise.
     */
    open protected fun checkNestedFragmentsForResult(
        requestCode: Int,
        resultCode: Int, data: Intent?
    ): Boolean {
        val id = mRequestCodes.get(requestCode)
        if (id == 0)
            return false

        mRequestCodes.delete(requestCode)

        val fragments = childFragmentManager.fragments ?: return false

        for (fragment in fragments) {
            if (fragment.hashCode() == id) {
                fragment.onActivityResult(requestCode, resultCode, data)
                return true
            }
        }

        return false
    }// checkNestedFragmentsForResult()


    open fun getAppCompatActivity(): AppCompatActivity {
        return activity as AppCompatActivity
    }

    open protected fun setActionBarTitle(title: CharSequence) {
        if (isAdded) {
            getAppCompatActivity().supportActionBar!!.title = title
        }
    }


    open fun isDirectlyUnderActivity(): Boolean {
        return parentFragment == null
    }

    open protected fun getCurrentFragmentManager(): FragmentManager? {
        return if (!isDirectlyUnderActivity()) {
            parentFragment?.childFragmentManager
        } else {
            activity?.supportFragmentManager
        }
    }
}