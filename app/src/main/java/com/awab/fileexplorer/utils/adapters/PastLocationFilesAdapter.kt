package com.awab.fileexplorer.utils.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awab.fileexplorer.R
import com.awab.fileexplorer.databinding.FileItemBinding
import com.awab.fileexplorer.utils.bindFileItem
import com.awab.fileexplorer.utils.data.data_models.FileDataModel

class PastLocationFilesAdapter(val context: Context, var files: List<FileDataModel>, val listener: LocationFilesListener) :
    RecyclerView.Adapter<PastLocationFilesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FileItemBinding.inflate(LayoutInflater.from(parent.context),parent, false)

        binding.root.layoutParams.height =
            parent.context.resources.getDimension(R.dimen.pick_Location_file_item_height).toInt()

        binding.tvFileName.textSize =
            parent.context.resources.getDimension(R.dimen.pick_Location_file_name_text_size)

        binding.tvFileSize.textSize = 0F

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(files[position])
    }

    override fun getItemCount() = files.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newFiles: List<FileDataModel>) {
        files = newFiles
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding:FileItemBinding):RecyclerView.ViewHolder(binding.root){

        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION){
                    listener.onFileClick(files[adapterPosition])
                }
            }
        }
        fun bind(item: FileDataModel){
            bindFileItem(context, item, binding,false)
        }
    }

    interface LocationFilesListener{
        fun onFileClick(file: FileDataModel)
    }
}