package com.awab.fileexplorer.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awab.fileexplorer.R
import com.awab.fileexplorer.adapters.utils.*
import com.awab.fileexplorer.databinding.FileItemBinding
import com.awab.fileexplorer.model.data_models.FileModel
import com.awab.fileexplorer.presenter.contract.SupPresenter


class SearchAdapter(val context: Context, val presenter: SupPresenter) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {


    private lateinit var currentList: List<FileModel>
    /**
     * the search text that the user has entered
     */
    var searchText = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FileItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    override fun getItemCount() = currentList.size


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

    @SuppressLint("NotifyDataSetChanged")
    fun setItemsList(list: List<FileModel>, searchText: String = "") {
        this.currentList = list
        this.searchText = searchText
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: FileItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION)
                    presenter.onFileClick(currentList[adapterPosition])
            }
            binding.root.setOnLongClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION)
                    presenter.onFileLongClick(currentList[adapterPosition])
                //  so the cilk event doesn't go to anther view in the tree
                true
            }
        }

        fun bind(item: FileModel) {
            bindFileItem(context, item, binding, presenter.actionModeOn)
            binding.tvFileName.text = getSpannableColoredString(context, item.name, searchText, R.color.search_Text_color)
        }
    }
}