package com.bodybank.ui.history

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.bodybank.ui.R
import kotlinx.android.synthetic.main.view_history_detail_entry_cell.view.*

class EstimationHistoryDetailEntryCell : FrameLayout {
    constructor(context: Context) : super(context) {
        LayoutInflater.from(context).inflate(R.layout.view_history_detail_entry_cell, this)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        LayoutInflater.from(context).inflate(R.layout.view_history_detail_entry_cell, this)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleArr: Int) : super(context, attrs, defStyleArr) {
        LayoutInflater.from(context).inflate(R.layout.view_history_detail_entry_cell, this)
    }

    var name: String? = null
        set(value) {
            field = value
            post {
                titleLabel.text = value
            }
        }

    fun setValueAndUnit(value: Double, unit: String?) {
        post {
            valueLabel.text = String.format("%.1f%s", value, unit ?: "")
        }
    }

    fun setValueAndUnit(value: Int, unit: String?) {
        post {
            valueLabel.text = String.format("%d%s", value, unit ?: "")
        }
    }


}