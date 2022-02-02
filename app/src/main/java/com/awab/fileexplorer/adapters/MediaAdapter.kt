package com.awab.fileexplorer.adapters


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.awab.fileexplorer.R
import com.awab.fileexplorer.databinding.FileItemBinding
import com.awab.fileexplorer.model.data_models.MediaItemModel
import com.awab.fileexplorer.model.types.MimeType
import com.awab.fileexplorer.presenter.contract.MediaPresenterContract
import com.bumptech.glide.Glide

class MediaAdapter : RecyclerView.Adapter<MediaAdapter.ViewHolder>() {
    lateinit var mContext: Context
    lateinit var mediaPresenter: MediaPresenterContract

    var currentList = listOf<MediaItemModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FileItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(currentList[position])

    override fun getItemCount(): Int = currentList.size

    fun setContext(context: Context) {
        mContext = context
    }

    fun setPresenter(presenter: MediaPresenterContract) {
        this.mediaPresenter = presenter
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setList(list: List<MediaItemModel>) {
        this.currentList = list
        notifyDataSetChanged()
    }

    fun selectOrUnselectItem(item: MediaItemModel) {
        for (i in currentList) {
            if (i == item) {
                i.selected = !i.selected
                notifyItemChanged(currentList.indexOf(i))
                break
            }
        }
    }

    fun getSelectedItems(): List<MediaItemModel> {
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

        fun bind(item: MediaItemModel) {
            if (mediaPresenter.actionModeOn && item.selected) {
                binding.root.setBackgroundColor(ContextCompat.getColor(mContext, R.color.light_gray))
            } else
                binding.root.setBackgroundColor(Color.WHITE)

            binding.tvFileName.text = item.name
            binding.tvFileSize.text = item.size

            //  show the arrow in the video image
            binding.ivFileImagePlayArrow.visibility = if (item.type == MimeType.VIDEO) View.VISIBLE
            else View.GONE

            when (item.type) {
                MimeType.IMAGE -> {
                    Glide
                        .with(mContext)
                        .load(item.uri)
                        .placeholder(R.drawable.ic_default_image_file)
                        .into(binding.ivFileImage)
                }
                MimeType.VIDEO -> {
                    Glide
                        .with(mContext)
                        .load(item.uri)
                        .placeholder(R.drawable.ic_default_video_file)
                        .into(binding.ivFileImage)
                }
                MimeType.AUDIO -> {
                    Glide
                        .with(mContext)
                        .load(item.uri)
                        .placeholder(R.drawable.ic_default_audio_file)
                        .into(binding.ivFileImage)
                }
                MimeType.PDF -> {
                    binding.ivFileImage.setImageResource(R.drawable.ic_default_pdf_file)
                }
                MimeType.HTML -> {
                    binding.ivFileImage.setImageResource(R.drawable.ic_default_html_file)
                }
                MimeType.TEXT -> {
                    binding.ivFileImage.setImageResource(R.drawable.ic_default_text_file)
                }
                else -> {
                }
            }
        }
    }
}