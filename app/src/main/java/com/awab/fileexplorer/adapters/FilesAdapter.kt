package com.awab.fileexplorer.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.awab.fileexplorer.adapters.utils.bindFileItem
import com.awab.fileexplorer.databinding.FileItemBinding
import com.awab.fileexplorer.model.data_models.FileModel
import com.awab.fileexplorer.presenter.contract.SupPresenter

class FilesAdapter(val mContext: Context, val presenter: SupPresenter) : ListAdapter<FileModel, FilesAdapter.ViewHolder>(DiffCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FileItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    fun selectOrUnSelect(file: FileModel) {
        for (i in currentList) {
            if (i == file) {
                i.selected = !i.selected
                notifyItemChanged(currentList.indexOf(i))
                break
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun selectAll() {
        currentList.forEach {
            it.selected = true
        }
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<FileModel> {
        return currentList.filter { it.selected }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun stopActionMode() {
        currentList.forEach {
            it.selected = false
        }
        notifyDataSetChanged()
    }

    fun fItem(): FileModel {
        return currentList.first()
    }

    inner class ViewHolder(private val binding: FileItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    presenter.onFileClick(getItem(adapterPosition))
                }
            }
            binding.root.setOnLongClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    presenter.onFileLongClick(getItem(adapterPosition))
                }
                true
            }
        }

        fun bind(item: FileModel) {
            bindFileItem(mContext, item, binding, presenter.actionModeOn)
        }
    }

    class DiffCallBack : DiffUtil.ItemCallback<FileModel>() {
        override fun areItemsTheSame(oldItem: FileModel, newItem: FileModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: FileModel, newItem: FileModel): Boolean {
            return oldItem == newItem
        }
    }
}