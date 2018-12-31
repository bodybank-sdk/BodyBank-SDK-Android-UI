package com.bodybank.ui.history

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.bodybank.estimation.EstimationRequest
import com.bodybank.ui.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.github.ybq.android.spinkit.SpinKitView
import com.stfalcon.frescoimageviewer.ImageViewer
import me.relex.circleindicator.CircleIndicator

open class EstimationHistoryDetailImageCell : FrameLayout {

    constructor(context: Context) : super(context) {
        LayoutInflater.from(context).inflate(R.layout.view_history_detail_image_cell, this)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        LayoutInflater.from(context).inflate(R.layout.view_history_detail_image_cell, this)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleArr: Int) : super(context, attrs, defStyleArr) {
        LayoutInflater.from(context).inflate(R.layout.view_history_detail_image_cell, this)
    }

    var adapter: Adapter? = null

    override fun onFinishInflate() {
        super.onFinishInflate()
        adapter = Adapter()
        findViewById<ViewPager>(R.id.viewPager)?.let { viewPager ->
            viewPager.adapter = adapter
            findViewById<CircleIndicator>(R.id.pageControl)?.let {
                it.setViewPager(viewPager)
            }
        }

    }

    var request: EstimationRequest? = null
        set(value) {
            field = value
            adapter?.notifyDataSetChanged()
        }

    open inner class Adapter : PagerAdapter() {
        override fun isViewFromObject(p0: View, p1: Any): Boolean {
            return p0 == p1
        }

        override fun getCount(): Int {
            return request?.let {
                2
            } ?: {
                0
            }()
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val itemView = LayoutInflater.from(context).inflate(R.layout.view_image_pager_item, container, false)
            val imageView = itemView.findViewById(R.id.imageView) as ImageView
            container.addView(itemView)

            val remoteImageProxy = if (position == 0) {
                request?.frontImage
            } else {
                request?.sideImage
            }
            Thread {
                remoteImageProxy?.downloadableURL?.toString()?.let {
                    Uri.parse(it)?.let { uri ->
                        post {
                            findViewById<SpinKitView>(R.id.spinKit).visibility = View.VISIBLE
                            Glide.with(context).load(uri).addListener(object : RequestListener<Drawable> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    findViewById<SpinKitView>(R.id.spinKit).visibility = View.GONE
                                    return true
                                }

                                override fun onResourceReady(
                                    resource: Drawable?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    dataSource: DataSource?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    findViewById<SpinKitView>(R.id.spinKit).visibility = View.GONE
                                    imageView.setImageDrawable(resource)
                                    return true
                                }
                            }).into(imageView)
                            itemView.setOnClickListener {
                                ImageViewer.Builder(context, listOf(uri)).show()
                            }
                        }
                    }
                }
            }.start()
            return itemView

        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }
    }


}