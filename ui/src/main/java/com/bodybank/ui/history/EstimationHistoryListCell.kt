package com.bodybank.ui.history

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.bodybank.estimation.EstimationRequest
import com.bodybank.ui.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.view_history_list_cell.view.*
import java.text.SimpleDateFormat

open class EstimationHistoryListCell : FrameLayout {
    companion object {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    }

    constructor(context: Context) : super(context) {
        LayoutInflater.from(context).inflate(R.layout.view_history_list_cell, this)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        LayoutInflater.from(context).inflate(R.layout.view_history_list_cell, this)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleArr: Int) : super(context, attrs, defStyleArr) {
        LayoutInflater.from(context).inflate(R.layout.view_history_list_cell, this)
    }


    var request: EstimationRequest? = null
        set(value) {
            field = value
            Thread {
                value?.frontImageThumb?.downloadableURL?.toString()?.let {
                    Uri.parse(it)?.let {
                        post {
                            spinKit.visibility = View.VISIBLE
                            Glide.with(this).load(it).addListener(object : RequestListener<Drawable> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    spinKit.visibility = View.GONE
                                    return true
                                }

                                override fun onResourceReady(
                                    resource: Drawable?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    dataSource: DataSource?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    imageView.setImageDrawable(resource)
                                    spinKit.visibility = View.GONE
                                    return true
                                }
                            }).into(imageView)
                        }
                    }
                } ?: {
                    post {
                        imageView.setImageURI(null)
                    }
                }()
            }.start()

            post {
                value?.createdAt?.let {
                    createdAtLabel?.text = dateFormat.format(it)
                }
                statusLabel.text = value?.status?.name
            }
        }
}