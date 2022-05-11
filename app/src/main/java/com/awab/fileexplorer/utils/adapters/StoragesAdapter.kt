package com.awab.fileexplorer.utils.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awab.fileexplorer.databinding.StorageItemBinding
import com.awab.fileexplorer.utils.data.data_models.StorageDataModel

class StoragesAdapter(var list: Array<StorageDataModel> = arrayOf(), val onClick: (StorageDataModel) -> Unit) :
    RecyclerView.Adapter<StoragesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = StorageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: Array<StorageDataModel>) {
        this.list = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: StorageItemBinding) : RecyclerView.ViewHolder(binding.root){

        init{
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION){
                    val item = list[adapterPosition]
                    onClick.invoke(item)
                }
            }
        }

        fun bind(item: StorageDataModel) {
            binding.apply {

                val percentage = (item.totalSizeBytes - item.freeSizeBytes).
                div(item.totalSizeBytes.toFloat()) * 100
                val percentageString = String.format("%.0f", percentage) + '%'
                tvProgressPercentage.text = percentageString

                pbSizeProgressBar.max = 100
                pbSizeProgressBar.progress = percentage.toInt()

                tvStorageName.text = item.name
                tvStorageSize.text = item.size
            }
        }
    }
}