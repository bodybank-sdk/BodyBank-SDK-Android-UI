package com.bodybank.ui.misc

import android.content.Context
import android.util.TypedValue

fun Context.dipToPixels(dipValue: Float) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, resources.displayMetrics)
