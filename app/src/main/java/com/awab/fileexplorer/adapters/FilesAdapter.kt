package com.awab.fileexplorer.adapters

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.awab.fileexplorer.R
import com.awab.fileexplorer.databinding.FileItemBinding
import com.awab.fileexplorer.model.data_models.AppIconModelData
import com.awab.fileexplorer.model.data_models.FileModel
import com.awab.fileexplorer.model.types.FileType
import com.awab.fileexplorer.model.types.MimeType
import com.awab.fileexplorer.presenter.contract.FilesListPresenterContract
import com.bumptech.glide.Glide
import java.io.File

class FilesAdapter : ListAdapter<FileModel, FilesAdapter.ViewHolder>(DiffCallBack()) {
    private lateinit var mFilesListPresenter: FilesListPresenterContract
    private lateinit var mContext: Context

    val modeOn: Boolean
        get() = mFilesListPresenter.actionModeOn

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FileItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    fun setContext(context: Context) {
        mContext = context
    }

    fun setPresenter(filesListPresenterContract: FilesListPresenterContract) {
        mFilesListPresenter = filesListPresenterContract
    }

    fun selectOrUnSelect(file: FileModel) {
        currentList.forEachIndexed { idx, it ->
            if (it == file) {
                it.selected = !it.selected
                notifyItemChanged(idx)
            }
        }
    }

    fun stopActionMode() {
        currentList.forEach {
            it.selected = false
        }
        notifyDataSetChanged()
    }

    fun selectAll() {
        currentList.forEach {
            it.selected = true
        }
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<FileModel> {
        return currentList.filter { it.selected }
    }

    inner class ViewHolder(private val binding: FileItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    mFilesListPresenter.onFileClick(getItem(adapterPosition))
                }
            }
            binding.root.setOnLongClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    mFilesListPresenter.onFileLongClick(getItem(adapterPosition))
                }
                true
            }
        }

        fun bind(item: FileModel) {
            if (modeOn && item.selected) {
//                animating the selected items
                val anim = Anim(24, binding.selected)
                anim.duration = 200
                binding.selected.animation = anim
            } else
                binding.selected.layoutParams.width = 0

            binding.tvFileName.text = item.name

            //  show the arrow in the video image
            binding.ivFileImagePlayArrow.visibility = if (item.mimeType == MimeType.VIDEO) View.VISIBLE
            else View.GONE

            if (item.type == FileType.FILE) {
                binding.tvFileSize.text = item.size
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

                    MimeType.APPLICATION -> {
                        val appIconModelData = AppIconModelData(item.path, File(item.path).isFile)
                        Glide.with(mContext)
                            .load(appIconModelData)
                            .placeholder(R.drawable.ic_default_application_file)
                            .into(binding.ivFileImage)
                    }

                    MimeType.AUDIO -> binding.ivFileImage.setImageResource(R.drawable.ic_default_audio_file)

                    MimeType.TEXT -> binding.ivFileImage.setImageResource(R.drawable.ic_default_text_file)

                    MimeType.HTML -> binding.ivFileImage.setImageResource(R.drawable.ic_default_html_file)

                    MimeType.PDF -> binding.ivFileImage.setImageResource(R.drawable.ic_default_pdf_file)

                    MimeType.UNKNOWN -> binding.ivFileImage.setImageResource(R.drawable.ic_default_file)
                }
            } else if (item.type == FileType.DIRECTORY) {
                binding.tvFileSize.text = ""
                val resource = if (item.isEmpty) R.drawable.ic_empty_folder else R.drawable.ic_folder
                binding.ivFileImage.setImageResource(resource)
            }
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

    class Anim(val width: Int, val view: View) : Animation() {
        private val startWidth = view.width

        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            val newWidth = startWidth + ((width - startWidth) * interpolatedTime).toInt()
            view.layoutParams.width = newWidth
            view.requestLayout()
            super.applyTransformation(interpolatedTime, t)
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }
}