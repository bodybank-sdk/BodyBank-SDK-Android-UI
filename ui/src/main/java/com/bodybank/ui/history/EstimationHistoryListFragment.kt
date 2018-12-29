package com.bodybank.ui.history

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bodybank.estimation.EstimationRequest
import com.bodybank.ui.misc.BaseFragment

class EstimationHistoryListFragment : BaseFragment() {
    inner class Adapter : RecyclerView.Adapter<Adapter.ViewHolder>(), View.OnClickListener {
        var requests: MutableList<EstimationRequest> = mutableListOf()
        var loading = false
        var loadingFinished = false

        inner open class ViewHolder(v: View) : RecyclerView.ViewHolder(v)


        inner class ContentViewHolder(internal var mCell: EstimationHistoryListCell) : ViewHolder(mCell) {

            init {
                mCell.setOnClickListener(this@Adapter)
            }
        }

        override fun getItemViewType(position: Int): Int {
            return 0
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            val cell = LayoutInflater.from(getAppCompatActivity()).inflate(
                R.layout.sample_result_table_cell,
                parent,
                false
            ) as EstimationHistoryListCell
            cell.setOnClickListener(this)
            return ContentViewHolder(cell)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (holder is ContentViewHolder) {
                val cell = holder.mCell
                cell.request = requests[position]
                if (requests.size > 0 && requests.size - 1 == position) {
                    if (!loadingFinished && !loading) {
                        loadNext()
                    }
                }
            }
        }

        fun loadNext() {

        }


        override fun getItemCount(): Int {
            return requests.size
        }

        override fun onClick(view: View) {
            val cell = view as EstimationHistoryListCell
            cell.request?.let {
                //TODO show detail
            }
        }


    }
}