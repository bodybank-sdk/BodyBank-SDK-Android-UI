package com.bodybank.ui.camera

import android.support.design.widget.BottomSheetDialog
import com.bodybank.ui.R
import kotlinx.android.synthetic.main.fragment_picker.*
import kotlinx.android.synthetic.main.view_unit_picker.view.*

class HeightPickerFramgent : BasePickerFragment() {
    override var defaultValue: Double
        get() = 150.0
        set(value) {}

    open var isFeet: Boolean
        get() = unit == "ft"
        set(value) = if (value) {
            unit = "ft"
        } else {
            unit = "cm"
        }

    open var heightInCm: Double = 0.0
        get() = if (isFeet) {
            val cmPerFeet = 30.48
            val cmPerInch = cmPerFeet / 12.0
            val feet_inch = valueEditText.text.toString().split(".")
            val feet = feet_inch[0].toDouble()
            val inch = feet_inch[1].toDouble()
            val feetsInCm = feet * cmPerFeet
            val inchesInCm = inch * cmPerInch
            feetsInCm + inchesInCm
        } else {
            valueEditText.text.toString().toDouble()
        }

    override fun showUnitSelector() {
        context?.let {
            val dialog = BottomSheetDialog(it)
            val view = layoutInflater.inflate(R.layout.view_unit_picker, null)
            dialog.setContentView(view)
            view.pickerElement1?.let {
                it.text = "cm"
                it.setOnClickListener {
                    unit = "cm"
                    dialog.hide()
                }

                view.pickerElement2?.let {
                    it.text = "ft"
                    it.setOnClickListener {
                        unit = "ft"
                        dialog.hide()
                    }
                }
            }
            dialog.show()

        }
    }
}