package com.bodybank.ui.tutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bodybank.ui.R
import com.bodybank.ui.misc.BaseFragment
import kotlinx.android.synthetic.main.fragment_tutorial_page.*

class TutorialPageFragment : BaseFragment() {

    open var tutorialImageResourceId: Int? = null
    open var tutorialText: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tutorial_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tutorialImageResourceId?.let {
            if (it > 0) {
                imageView?.setImageResource(it)
            }
        }
        tutorialText?.let {
            textView?.text = it
        }
    }
}