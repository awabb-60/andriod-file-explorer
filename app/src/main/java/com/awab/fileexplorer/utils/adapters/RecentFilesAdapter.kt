package com.awab.fileexplorer.utils.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awab.fileexplorer.R
import com.awab.fileexplorer.databinding.RecentFileLayoutBinding

class RecentFilesAdapter(val list: List<Int>) : RecyclerView.Adapter<RecentFilesAdapter.ViewHolder>() {

    private val MARGIN_SIZE = 10
    private val PEAR_ROW = 3

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecentFileLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        val itemWidth = (parent.width / PEAR_ROW) - (MARGIN_SIZE * 2)

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

    inner class ViewHolder(val binding: RecentFileLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Int) {
            binding.apply {
                tvRecentFileName.text = "name$item"
                tvRecentFileImage.setImageResource(R.drawable.ic_default_file)
            }
        }
    }
}
