package com.bodybank.ui.camera

class AgePickerFragment : BasePickerFragment() {

    override var defaultValue: Double
        get() = 25.0
        set(_) {}

    override fun correctedValueString(value: Double): String {
        return String.format("%.0f", value)
    }

    override fun showUnitSelector() {

    }
}