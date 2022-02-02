package com.awab.fileexplorer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awab.fileexplorer.model.data_models.BreadcrumbsModel
import com.awab.fileexplorer.databinding.BItemBinding
import com.awab.fileexplorer.model.utils.listeners.BreadcrumbsListener

class BreadcrumbsAdapter() : RecyclerView.Adapter<BreadcrumbsAdapter.ViewHolder>() {
    lateinit var mBreadcrumbsListener: BreadcrumbsListener
    var list = mutableListOf<BreadcrumbsModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = BItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size

    fun setListener(listener: BreadcrumbsListener) {
        mBreadcrumbsListener = listener
    }

    fun add(breadcrumbsModel: BreadcrumbsModel) {
        list.add(breadcrumbsModel)
        notifyItemInserted(list.lastIndex)
        notifyItemChanged(list.lastIndex - 1)
    }

    fun removeLast() {
//        removing the last and updating the previews item
        list.removeLast()
        notifyItemRemoved(list.lastIndex + 1)
        notifyItemChanged(list.lastIndex)
    }

    inner class ViewHolder(private val binding: BItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
//              doesn't work on the last item or no item
                if (adapterPosition != RecyclerView.NO_POSITION && list[adapterPosition] != list.last()) {
                    mBreadcrumbsListener.onBreadcrumbsItemClicked(list[adapterPosition])
                }
            }
        }

        fun bind(item: BreadcrumbsModel) {
            binding.nameTextView.text = item.name
            val alpha = if (list.indexOf(item) != list.lastIndex) {
                0.5F
            } else {
                1F
            }
            binding.nameTextView.alpha = alpha
            binding.arrowTextView.alpha = alpha

        }
    }

    interface OnItemClickListener {
        fun onItemClicked(position: Int)
    }
}