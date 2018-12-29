package com.bodybank.ui.camera

import android.support.design.widget.BottomSheetDialog
import com.bodybank.ui.R
import kotlinx.android.synthetic.main.fragment_picker.*
import kotlinx.android.synthetic.main.view_unit_picker.view.*

class WeightPickerFramgent : BasePickerFragment() {
    override var defaultValue: Double
        get() = 150.0
        set(value) {}

    open var isPound: Boolean
        get() = unit == "lbs"
        set(value) = if (value) {
            unit = "lbs"
        } else {
            unit = "kg"
        }

    open var weightInKg: Double = 0.0
        get() =
            if (isPound) {
                val stringRepresentation = valueEditText.text.toString()
                val pound_oz = stringRepresentation.split(".")
                val pound = pound_oz[0].toDouble()
                val oz = pound_oz[1].toDouble()
                val kgPerPound = 0.4535924
                val ozPerPound = kgPerPound / 12.0
                val kg = pound * kgPerPound + oz * ozPerPound
                kg
            } else {
                valueEditText.text.toString().toDouble()
            }

    override fun showUnitSelector() {
        context?.let {
            val dialog = BottomSheetDialog(it)
            val view = layoutInflater.inflate(R.layout.view_unit_picker, null)
            dialog.setContentView(view)
            view.pickerElement1?.let {
                it.text = "kg"
                it.setOnClickListener {
                    unit = "kg"
                    dialog.hide()
                }

                view.pickerElement2?.let {
                    it.text = "lbs"
                    it.setOnClickListener {
                        unit = "lbs"
                        dialog.hide()
                    }
                }
            }
            dialog.show()

        }
    }
}