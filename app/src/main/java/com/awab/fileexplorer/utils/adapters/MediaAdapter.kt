package com.awab.fileexplorer.utils.adapters


import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awab.fileexplorer.R
import com.awab.fileexplorer.databinding.FileItemBinding
import com.awab.fileexplorer.presenter.contract.MediaPresenterContract
import com.awab.fileexplorer.utils.bindFileItem
import com.awab.fileexplorer.utils.data.data_models.MediaItemDataModel
import com.awab.fileexplorer.utils.getSpannableColoredString

class MediaAdapter(val context: Context, val mediaPresenter: MediaPresenterContract) : RecyclerView.Adapter<MediaAdapter.ViewHolder>() {

    var searchText = ""

    var currentList = listOf<MediaItemDataModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FileItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(currentList[position])

    override fun getItemCount(): Int = currentList.size

    @SuppressLint("NotifyDataSetChanged")
    fun setList(list: List<MediaItemDataModel>) {
        this.currentList = list
        notifyDataSetChanged()
    }

    fun selectOrUnselectItem(item: MediaItemDataModel) {
        for (i in currentList) {
            if (i == item) {
                i.selected = !i.selected
                notifyItemChanged(currentList.indexOf(i))
                break
            }
        }
    }

    fun getSelectedItems(): List<MediaItemDataModel> {
        return currentList.filter { it.selected }
    }

    fun unselectAll() {
        currentList.forEachIndexed { idx, it ->
            if (it.selected) {
                it.selected = false
                notifyItemChanged(idx)
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

    inner class ViewHolder(private val binding: FileItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    mediaPresenter.mediaItemClicked(currentList[adapterPosition])
                }
            }

            binding.root.setOnLongClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    mediaPresenter.mediaItemLongClicked(currentList[adapterPosition])
                }
                true
            }
        }

        fun bind(item: MediaItemDataModel) {
            bindFileItem(context, item, binding, mediaPresenter.actionModeOn)
            if (searchText.isNotEmpty()){
                binding.tvFileName.text = getSpannableColoredString(context,
                    binding.tvFileName.text.toString(),searchText, R.color.search_Text_color)
            }
        }
    }
}