package com.awab.fileexplorer.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awab.fileexplorer.databinding.PickStorageBinding
import com.awab.fileexplorer.model.data_models.StorageModel

class PastLocationStoragesAdapter(val storages: Array<StorageModel>,
val listener:LocationStoragesListener) :
    RecyclerView.Adapter<PastLocationStoragesAdapter.ViewHolder>() {

    var currentSelectedPos = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PickStorageBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = storages.size

    fun getCurrentStorage(): StorageModel =
        storages[currentSelectedPos]

    @SuppressLint("NotifyDataSetChanged")
    inner class ViewHolder(val binding:PickStorageBinding):RecyclerView.ViewHolder(binding.root){

        init {
            binding.storageName.setOnClickListener {
                if(adapterPosition != RecyclerView.NO_POSITION){
                    currentSelectedPos = adapterPosition
                    listener.storageChanged(storages[adapterPosition])
                    notifyDataSetChanged()
                }
            }
        }
        fun bind(pos:Int){
            val item = storages[pos]
            binding.storageName.text = item.name
            if (pos == currentSelectedPos)
                binding.root.setCardBackgroundColor(Color.LTGRAY)
            else
                binding.root.setCardBackgroundColor(Color.WHITE)

        }
    }

    interface LocationStoragesListener{
        fun storageChanged(storage:StorageModel)
    }
}