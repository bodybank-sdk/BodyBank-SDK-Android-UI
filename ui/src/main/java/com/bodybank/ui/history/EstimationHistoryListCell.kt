package com.bodybank.ui.history

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.bodybank.estimation.EstimationRequest
import com.bodybank.ui.R
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.view_history_list_cell.view.*
import java.lang.Exception
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
                            Picasso.get().load(it)?.into(imageView, object : Callback {
                                override fun onSuccess() {
                                    spinKit.visibility = View.GONE
                                }

                                override fun onError(e: Exception?) {

                                    spinKit.visibility = View.GONE
                                }
                            })
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