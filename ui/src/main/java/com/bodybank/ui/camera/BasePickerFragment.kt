package com.bodybank.ui.camera

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bodybank.ui.R
import com.bodybank.ui.misc.BaseFragment
import com.bodybank.ui.misc.ViewPressEffectHelper
import kotlinx.android.synthetic.main.fragment_picker.*
import java.lang.Exception

public abstract class BasePickerFragment : BaseFragment() {

    public interface Delegate {
        fun onFinishPickerFragment(fragment: BasePickerFragment)
        fun onCancelPickerFragment(fragment: BasePickerFragment)
    }

    open var initialValueString: String? = null
    open var unit: String? = null
    abstract var defaultValue: Double
    open var delegate: Delegate? = null

    open var currentValue: Double = 0.0
        get() {
            return valueEditText.text?.let {
                return try {
                    if (it.toString().toDouble() != 0.0) {
                        it.toString().toDouble()
                    } else {
                        defaultValue
                    }
                } catch (e: Exception) {
                    defaultValue
                }
            } ?: {
                defaultValue
            }()
        }

    open override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_picker, container, false)
    }

    open override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (initialValueString == "-") {
            saveButton.isEnabled = false
        }
        unitLabel.text = unit
        saveButton?.setOnClickListener { onClickSaveButton() }
        closeButton?.setOnClickListener { onClickCloseButton() }
        plusButton?.setOnClickListener { onClickPlusButton() }
        minusButton?.setOnClickListener { onClickMinusButton() }

        listOf<View?>(saveButton, closeButton, plusButton, minusButton).forEach { view ->
            view?.let {
                ViewPressEffectHelper.attach(it)
            }
        }
        valueEditText?.setOnFocusChangeListener { view, b ->
            inflateDefaultValue()
        }
        valueEditText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                inflateDefaultValue()
            }
        })
        valueEditText.keyListener = DigitsKeyListener.getInstance("0123456789.")
    }

    abstract fun showUnitSelector()

    open fun inflateDefaultValue() {
        if (valueEditText.text.toString() == "-") {
            valueEditText.setText(correctedValueString(currentValue))
            saveButton.isEnabled = true
        }
    }

    open fun correctedValueString(value: Double): String {
        return String.format("%.2f", value)
    }


    open fun onClickMinusButton() {
        inflateDefaultValue()
        val value = valueEditText.text.toString().toDouble()
        val nextValue = Math.max(value - 1, 0.0)
        valueEditText.setText(correctedValueString(nextValue))
    }

    open fun onClickPlusButton() {
        inflateDefaultValue()
        val value = valueEditText.text.toString().toDouble()
        val nextValue = Math.max(value + 1, 0.0)
        valueEditText.setText(correctedValueString(nextValue))
    }

    open fun onClickCloseButton() {
        delegate?.onCancelPickerFragment(this)
    }

    open fun onClickSaveButton() {
        delegate?.onFinishPickerFragment(this)
    }


    open fun setInitialValue(valueString: String, unit: String) {
        initialValueString = valueString
        this.unit = unit
    }
}