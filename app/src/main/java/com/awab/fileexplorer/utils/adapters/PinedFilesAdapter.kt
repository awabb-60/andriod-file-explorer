package com.awab.fileexplorer.utils.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awab.fileexplorer.databinding.PinedFileLayoutBinding
import com.awab.fileexplorer.utils.bindFileItem
import com.awab.fileexplorer.utils.data.data_models.FileDataModel

class PinedFilesAdapter(val context: Context) : RecyclerView.Adapter<PinedFilesAdapter.ViewHolder>() {

    companion object {
        const val MARGIN_SIZE = 10
        const val PER_ROW = 4
    }

    var list = listOf<FileDataModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PinedFileLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        val itemWidth = (parent.width / PER_ROW) - (MARGIN_SIZE * 2)

        val layoutParams = binding.root.layoutParams as ViewGroup.MarginLayoutParams

        layoutParams.width = itemWidth
        layoutParams.height = (itemWidth * 0.95).toInt()

        layoutParams.setMargins(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: List<FileDataModel>) {
        this.list = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: PinedFileLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FileDataModel) {
            bindFileItem(context, item, binding)
        }

    }
}
