package com.bodybank.ui.history

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bodybank.estimation.EstimationRequest
import com.bodybank.ui.R
import com.bodybank.ui.misc.BaseFragment
import com.bodybank.ui.misc.VerticalSpaceItemDecoration
import com.bodybank.ui.misc.dipToPixels
import com.facebook.drawee.backends.pipeline.Fresco
import kotlinx.android.synthetic.main.fragment_history_list.*
import java.text.SimpleDateFormat

open class EstimationHistoryDetailFragment : BaseFragment() {

    open class ResultEntry(val name: String, val value: Any?, val unit: String?)


    var request: EstimationRequest? = null
    var adapter: Adapter? = null
    var lengthUnit = "cm"
    var massUnit = "kg"
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_history_detail, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = Adapter()
        recyclerView.layoutManager = LinearLayoutManager(activity!!.applicationContext)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(VerticalSpaceItemDecoration(1))
        setHasOptionsMenu(true)
        if (!Fresco.hasBeenInitialized()) {
            Fresco.initialize(activity!!.applicationContext)
        }
    }

    internal enum class CellViewType {
        Image, Entry
    }

    inner class Adapter : RecyclerView.Adapter<EstimationHistoryDetailFragment.Adapter.ViewHolder>() {

        var entries: MutableList<ResultEntry> = mutableListOf()

        init {
            generateEntries()
            notifyDataSetChanged()
        }


        fun generateEntries() {
            entries.clear()
            request?.let {
                entries.add(ResultEntry("Height", it.height, lengthUnit))
                entries.add(ResultEntry("Weight", it.weight, massUnit))
                entries.add(ResultEntry("Age", it.age, null))
                entries.add(ResultEntry("Gender", it.gender?.name, null))
                it.result?.let { result ->
                    entries.add(ResultEntry("Neck", result.neckCircumference, lengthUnit))
                    entries.add(ResultEntry("Shoulder", result.shoulderWidth, lengthUnit))
                    entries.add(ResultEntry("Sleeve", result.sleeveLength, lengthUnit))
                    entries.add(ResultEntry("Bicep", result.bicepCircumference, lengthUnit))
                    entries.add(ResultEntry("Wrist", result.wristCircumference, lengthUnit))
                    entries.add(ResultEntry("Chest", result.chestCircumference, lengthUnit))
                    entries.add(ResultEntry("Waist", result.waistCircumference, lengthUnit))
                    entries.add(ResultEntry("High Hip", result.thighCircumference, lengthUnit))
                    entries.add(ResultEntry("Hip", result.hipCircumference, lengthUnit))
                    entries.add(ResultEntry("Thigh", result.thighCircumference, lengthUnit))
                    entries.add(ResultEntry("Mid Thigh", result.midThichCircumference, lengthUnit))
                    entries.add(ResultEntry("Knee", result.kneeCircumference, lengthUnit))
                    entries.add(ResultEntry("Inseam", result.inseamLength, lengthUnit))
                    entries.add(ResultEntry("Out seam", result.outseamLength, lengthUnit))
                    entries.add(ResultEntry("Total Length", result.totalLength, lengthUnit))
                }

                it.createdAt?.let { createdAt ->
                    activity?.actionBar?.title = simpleDateFormat.format(createdAt)
                }
            }

        }

        inner open class ViewHolder(v: View) : RecyclerView.ViewHolder(v)


        inner class ContentViewHolder(internal var mCell: View) : ViewHolder(mCell)

        override fun getItemViewType(position: Int): Int {
            return when (position) {
                0 -> CellViewType.Image.ordinal
                else -> CellViewType.Entry.ordinal
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            val cell = when (viewType) {
                CellViewType.Image.ordinal -> {
                    LayoutInflater.from(getAppCompatActivity()).inflate(
                        R.layout.sample_history_detail_image_cell,
                        parent,
                        false
                    )
                }
                else -> {
                    LayoutInflater.from(getAppCompatActivity()).inflate(
                        R.layout.sample_history_detail_entry_cell,
                        parent,
                        false
                    )
                }
            } as View

            return ContentViewHolder(cell)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (holder is ContentViewHolder) {
                val cell = holder.mCell
                when (position) {
                    0 -> {
                        (cell as? EstimationHistoryDetailImageCell)?.let {
                            it.request = request
                            val layoutParams = it.layoutParams
                            layoutParams.height = context!!.dipToPixels(300f).toInt()
                            it.layoutParams = layoutParams
                        }
                    }
                    else -> {
                        (cell as? EstimationHistoryDetailEntryCell)?.let {
                            val entry = entries[position - 1]
                            it.name = entry.name
                            entry.value?.let { value ->
                                it.setValueAndUnit(value, entry.unit)
                            }
                            val layoutParams = it.layoutParams
                            layoutParams.height = context!!.dipToPixels(50f).toInt()
                            it.layoutParams = layoutParams
                        }
                    }
                }
            }
        }

        override fun getItemCount(): Int {
            return entries.size + 1
        }

    }

}