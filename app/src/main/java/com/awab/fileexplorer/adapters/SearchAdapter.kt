package com.awab.fileexplorer.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.awab.fileexplorer.R
import com.awab.fileexplorer.databinding.FileItemBinding
import com.awab.fileexplorer.model.data_models.FileModel
import com.awab.fileexplorer.model.types.FileType
import com.awab.fileexplorer.model.types.MimeType
import com.awab.fileexplorer.model.utils.getApkIcon
import com.awab.fileexplorer.presenter.contract.SearchPresenterContract
import com.bumptech.glide.Glide


class SearchAdapter : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    private lateinit var list: List<FileModel>
    private lateinit var mSearchPresenter: SearchPresenterContract
    private lateinit var mContext: Context

    var searchText = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FileItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size

    @SuppressLint("NotifyDataSetChanged")
    fun setItemsList(list: List<FileModel>, searchText: String = "") {
        this.list = list
        this.searchText = searchText
        notifyDataSetChanged()
    }

    fun setContext(context: Context) {
        mContext = context
    }

    fun setPresenter(presenter: SearchPresenterContract) {
        mSearchPresenter = presenter
    }

    inner class ViewHolder(private val binding: FileItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION)
                    mSearchPresenter.onItemClicked(list[adapterPosition])
            }
            binding.root.setOnLongClickListener {
                true
            }
        }

        fun bind(item: FileModel) {
            binding.apply {
                tvFileName.text = getSpannableColoredString(item.name, searchText, R.color.search_Text_color)
                // show the arrow in the video image
                ivFileImagePlayArrow.visibility = if (item.mimeType == MimeType.VIDEO) View.VISIBLE
                else View.GONE

                if (item.type == FileType.FILE) {
                    tvFileSize.text = item.size
//                setting the image uri or the default file image
                    when (item.mimeType) {
                        MimeType.IMAGE -> {
                            Glide.with(mContext)
                                .load(item.uri)
                                .placeholder(R.drawable.ic_default_image_file)
                                .into(binding.ivFileImage)
                        }
                        MimeType.VIDEO -> {
                            Glide.with(mContext)
                                .load(item.uri)
                                .placeholder(R.drawable.ic_default_video_file)
                                .into(binding.ivFileImage)
                        }
                        MimeType.AUDIO -> binding.ivFileImage.setImageResource(R.drawable.ic_default_audio_file)
                        MimeType.TEXT -> binding.ivFileImage.setImageResource(R.drawable.ic_default_text_file)
                        MimeType.HTML -> binding.ivFileImage.setImageResource(R.drawable.ic_default_html_file)
                        MimeType.PDF -> binding.ivFileImage.setImageResource(R.drawable.ic_default_pdf_file)
                        MimeType.APPLICATION -> {
                            val apkIcon = getApkIcon(mContext, item.path)
                            Glide.with(mContext)
                                .load(apkIcon)
                                .placeholder(R.drawable.ic_default_application_file)
                                .into(binding.ivFileImage)
                        }
                        MimeType.UNKNOWN -> binding.ivFileImage.setImageResource(R.drawable.ic_default_file)
                    }
                } else if (item.type == FileType.DIRECTORY) {
                    tvFileSize.text = ""
                    val resource = if (item.isEmpty) R.drawable.ic_empty_folder else R.drawable.ic_folder
                    ivFileImage.setImageResource(resource)
                }
            }
        }
    }

    private fun getSpannableColoredString(string: String, coloredSegment: String, colorRes: Int): SpannableString {

        if (!string.lowercase().contains(coloredSegment.lowercase()))
            return SpannableString(string)

        val ss = SpannableString(string)
        val color = ContextCompat.getColor(mContext, colorRes)
        val foregroundColor = ForegroundColorSpan(color)
        val start = string.lowercase().indexOf(coloredSegment)
        val end = start + coloredSegment.length
        ss.setSpan(foregroundColor, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return ss
    }


}