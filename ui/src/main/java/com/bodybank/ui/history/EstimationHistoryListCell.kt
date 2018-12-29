package com.bodybank.ui.history

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.bodybank.estimation.EstimationRequest

open class EstimationHistoryListCell : FrameLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleArr: Int) : super(context, attrs, defStyleArr)

    var request: EstimationRequest? = null
        set(value) {
            field = value
            //TODO update UI
        }
}