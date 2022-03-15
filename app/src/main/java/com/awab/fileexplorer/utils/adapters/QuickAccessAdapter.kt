package com.awab.fileexplorer.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.awab.fileexplorer.databinding.QuickAccessFileLayoutBinding
import com.awab.fileexplorer.presenter.contract.HomePresenterContract
import com.awab.fileexplorer.utils.bindFileItem
import com.awab.fileexplorer.utils.data.data_models.FileDataModel

class QuickAccessAdapter(val context: Context, val presenter: HomePresenterContract) :
    ListAdapter<FileDataModel, QuickAccessAdapter.ViewHolder>(
        DiffCallBack()
    ) {

    companion object {
        const val MARGIN_SIZE = 6
        const val PER_ROW = 4
        var itemDimen = 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = QuickAccessFileLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        // updating the card height at the start
        if (itemDimen == 0)
            presenter.updateQuickAccessCardHeight(((parent.width / PER_ROW) * 2) + (MARGIN_SIZE * 2))

        itemDimen = (parent.width / PER_ROW) - (MARGIN_SIZE * 2)

        val layoutParams = binding.root.layoutParams as ViewGroup.MarginLayoutParams

        layoutParams.width = itemDimen
        layoutParams.height = (itemDimen * 0.95).toInt()

        layoutParams.setMargins(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(val binding: QuickAccessFileLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION)
                    presenter.quickAccessItemClicked(getItem(adapterPosition))
            }
        }

        fun bind(item: FileDataModel) {
            bindFileItem(context, item, binding)
        }
    }

    class DiffCallBack : DiffUtil.ItemCallback<FileDataModel>() {
        override fun areItemsTheSame(oldItem: FileDataModel, newItem: FileDataModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: FileDataModel, newItem: FileDataModel): Boolean {
            return oldItem == newItem
        }
    }
}
