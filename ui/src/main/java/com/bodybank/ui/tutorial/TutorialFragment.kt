package com.bodybank.ui.tutorial

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bodybank.ui.R
import com.bodybank.ui.misc.BaseFragment
import com.bodybank.ui.misc.ViewPressEffectHelper
import kotlinx.android.synthetic.main.fragment_tutorial.*
import kotlinx.android.synthetic.main.fragment_tutorial.view.*

open class TutorialFragment : BaseFragment() {
    open interface Delegate {
        fun onTutorialFragmentEnd(fragment: TutorialFragment)
    }

    open var delegate: Delegate? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tutorial, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentManager?.let { fragmentManager ->
            activity?.let { activity ->
                val adapter = Adapter(fragmentManager, activity)
                viewPager.adapter = adapter
                nextButton.setOnClickListener {
                    when (viewPager.currentItem) {
                        0, 1 -> {
                            viewPager.setCurrentItem(viewPager.currentItem, true)
                        }
                        2 -> {
                            delegate?.onTutorialFragmentEnd(this)
                        }
                    }
                }
                pageControl.setViewPager(viewPager)
                adapter.registerDataSetObserver(pageControl.dataSetObserver)
                ViewPressEffectHelper.attach(nextButton)
                viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                    override fun onPageScrollStateChanged(p0: Int) {

                    }

                    override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {

                    }

                    override fun onPageSelected(page: Int) {
                        when (page) {
                            2 -> {
                                activity?.runOnUiThread {
                                    nextButton.text = "Start"
                                }
                            }
                            else -> {
                                activity?.runOnUiThread {
                                    nextButton.text = "Next"
                                }
                            }
                        }
                    }
                })
            }

        }

    }

    open class Adapter(fragmentManager: FragmentManager, context: Context) :
        FragmentStatePagerAdapter(fragmentManager) {
        var messages: List<String> = arrayListOf()
        var images: MutableList<Int> = mutableListOf()

        init {
            messages = context.resources.getStringArray(R.array.tutorial_messages).toList()
            val typedImageArray = context.resources.obtainTypedArray(R.array.tutorial_images)
            for (i in 0..(messages.count() - 1)) {
                images.add(i, typedImageArray.getResourceId(i, -1))
            }
            typedImageArray.recycle()
        }

        override fun getItem(index: Int): Fragment {
            val fragment = TutorialPageFragment()
            fragment.tutorialText = messages[index]
            fragment.tutorialImageResourceId = images[index]
            return fragment
        }

        override fun getCount(): Int {
            return messages.count()
        }
    }

}