package com.bodybank.ui.camera

import android.os.Bundle
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.view.View
import kotlinx.android.synthetic.main.fragment_picker.*

class AgePickerFragment : BasePickerFragment() {

    override var defaultValue: Double
        get() = 25.0
        set(_) {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titleLabel?.text = "Age"
        valueEditText?.inputType = InputType.TYPE_CLASS_NUMBER
        valueEditText?.keyListener = DigitsKeyListener.getInstance("0123456789")
    }

    override fun correctedValueString(value: Double): String {
        return String.format("%.0f", value)
    }

    override fun showUnitSelector() {

    }

    open var age: Int = 0
        get() = valueEditText.text.toString().toInt()
}