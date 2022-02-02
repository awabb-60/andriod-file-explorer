package com.awab.fileexplorer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awab.fileexplorer.model.data_models.StorageModel
import com.awab.fileexplorer.databinding.StorageItemBinding

class StoragesAdapter(val onClick:(StorageModel) -> Unit) : RecyclerView.Adapter<StoragesAdapter.ViewHolder>() {

    lateinit var list:List<StorageModel>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = StorageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size


    fun set(list:List<StorageModel>){
        this.list = list
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

        fun bind(item: StorageModel) {
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