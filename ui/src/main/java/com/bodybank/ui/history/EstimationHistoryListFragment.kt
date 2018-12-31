package com.bodybank.ui.history

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bodybank.core.BodyBankEnterprise
import com.bodybank.estimation.EstimationRequest
import com.bodybank.ui.R
import com.bodybank.ui.misc.BaseFragment
import com.bodybank.ui.misc.VerticalSpaceItemDecoration
import kotlinx.android.synthetic.main.fragment_history_list.*

class EstimationHistoryListFragment : BaseFragment() {

    var adapter: Adapter? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_history_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = Adapter()
        recyclerView.layoutManager = LinearLayoutManager(activity!!.applicationContext)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(VerticalSpaceItemDecoration(1))

        adapter?.loadNext()
    }

    inner class Adapter : RecyclerView.Adapter<Adapter.ViewHolder>(), View.OnClickListener {
        var requests: MutableList<EstimationRequest> = mutableListOf()
        var loading = false
        var loadingFinished = false
        var shouldRefresh = false
        var nextToken: String? = null


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
                R.layout.sample_history_list_cell,
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

        fun refresh() {
            shouldRefresh = true
            loadingFinished = false
            loadNext()
        }

        fun loadNext() {
            if (loading) {
                return
            }
            loading = true
            BodyBankEnterprise.defaultTokenProvider()?.let {
                val token: String? = if (shouldRefresh) {
                    null
                } else {
                    nextToken
                }
                val limit = 20
                BodyBankEnterprise.listEstimationRequests(limit, token) { requests, nextToken, errors ->
                    errors?.let {
                        loading = false
                        activity?.runOnUiThread {
                            AlertDialog.Builder(activity!!)
                                .setMessage(errors.map { error -> error.message }.joinToString("\n"))
                                .setPositiveButton("OK") { _, _ ->
                                }.show()
                        }
                    } ?: {
                        this.nextToken = nextToken
                        this.loadingFinished = requests?.isEmpty() == true
                        activity?.runOnUiThread {
                            var previousLast = this.requests.count()
                            if (shouldRefresh) {
                                notifyItemRangeRemoved(0, this.requests.count())
                                this.requests.clear()
                                previousLast = 0
                            }
                            this.requests.addAll(requests!!)
                            notifyItemRangeInserted(previousLast, requests.count())
                            loading = false
                        }
                    }()
                }
            }
        }


        override fun getItemCount(): Int {
            return requests.size
        }

        override fun onClick(view: View) {
            val cell = view as EstimationHistoryListCell
            cell.request?.let {
                val detailFragment = EstimationHistoryDetailFragment()
                fragmentManager?.beginTransaction()?.add(detailFragment, "detail")?.commit()
            }
        }


    }
}