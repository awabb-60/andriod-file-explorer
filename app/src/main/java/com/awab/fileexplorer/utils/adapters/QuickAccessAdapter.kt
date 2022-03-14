package com.awab.fileexplorer.utils.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awab.fileexplorer.databinding.QuickAccessFileLayoutBinding
import com.awab.fileexplorer.presenter.contract.HomePresenterContract
import com.awab.fileexplorer.utils.bindFileItem
import com.awab.fileexplorer.utils.data.data_models.FileDataModel

class QuickAccessAdapter(val context: Context, val presenter: HomePresenterContract) :
    RecyclerView.Adapter<QuickAccessAdapter.ViewHolder>() {

    companion object {
        const val MARGIN_SIZE = 6
        const val PER_ROW = 4
    }

    private var itemDimen = 0
    private var count = 0

    private var list = listOf<FileDataModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = QuickAccessFileLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        itemDimen = (parent.width / PER_ROW) - (MARGIN_SIZE * 2)

        val layoutParams = binding.root.layoutParams as ViewGroup.MarginLayoutParams

        layoutParams.width = itemDimen
        layoutParams.height = (itemDimen * 0.95).toInt()

        layoutParams.setMargins(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE)
        return ViewHolder(binding)
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        if (count == 0) {
            presenter.setQuickAccessFilesCardHeight(getCardHeight())
            count++
        }
        super.onViewAttachedToWindow(holder)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: List<FileDataModel>) {
        this.list = list
        notifyDataSetChanged()
        if (list.isEmpty()) {
            presenter.setQuickAccessFilesCardHeight(0)
            count = 0
        }
    }

    private fun getCardHeight(): Int {
        return (itemDimen * 2) + (MARGIN_SIZE * 2)
    }

    inner class ViewHolder(val binding: QuickAccessFileLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FileDataModel) {
            bindFileItem(context, item, binding)
        }

    }
}
