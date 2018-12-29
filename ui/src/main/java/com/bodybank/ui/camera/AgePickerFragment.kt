package com.bodybank.ui.camera

import kotlinx.android.synthetic.main.fragment_picker.*

class AgePickerFragment : BasePickerFragment() {

    override var defaultValue: Double
        get() = 25.0
        set(_) {}

    override fun correctedValueString(value: Double): String {
        return String.format("%.0f", value)
    }

    override fun showUnitSelector() {

    }

    open var age: Int = 0
        get() = valueEditText.text.toString().toInt()
}