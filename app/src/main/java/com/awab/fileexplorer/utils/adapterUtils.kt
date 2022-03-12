package com.awab.fileexplorer.utils

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.content.ContextCompat
import com.awab.fileexplorer.R
import com.awab.fileexplorer.databinding.FileItemBinding
import com.awab.fileexplorer.utils.data.data_models.AlbumCoverDataModel
import com.awab.fileexplorer.utils.data.data_models.AppIconDataModel
import com.awab.fileexplorer.utils.data.data_models.FileDataModel
import com.awab.fileexplorer.utils.data.data_models.MediaItemDataModel
import com.awab.fileexplorer.utils.data.types.FileType
import com.awab.fileexplorer.utils.data.types.MimeType
import com.bumptech.glide.Glide
import java.io.File

fun bindFileItem(context: Context, item: FileDataModel, binding:FileItemBinding, modeOn:Boolean){
    if (modeOn && item.selected) {
        binding.selected.rotation = -90F
        binding.selected.animate().rotation(0F).setDuration(400L).start()
        binding.selected.layoutParams.width = binding.selected.layoutParams.height
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
                Glide.with(context)
                    .load(item.uri)
                    .placeholder(R.drawable.ic_default_image_file)
                    .into(binding.ivFileImage)
            }

            MimeType.VIDEO -> {
                Glide.with(context)
                    .load(item.uri)
                    .placeholder(R.drawable.ic_default_video_file)
                    .into(binding.ivFileImage)
            }

            MimeType.APPLICATION -> {
                val appIconModelData = AppIconDataModel(item.path, File(item.path).isFile)
                Glide.with(context)
                    .load(appIconModelData)
                    .placeholder(R.drawable.ic_default_application_file)
                    .into(binding.ivFileImage)
            }

            MimeType.AUDIO -> {
                val albumCoverModelData = AlbumCoverDataModel(item.name, File(item.path).isFile)
                Glide.with(context)
                    .load(albumCoverModelData)
                    .placeholder(R.drawable.ic_default_audio_file)
                    .into(binding.ivFileImage)
            }

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

fun bindFileItem(context: Context, item: MediaItemDataModel, binding:FileItemBinding, modeOn:Boolean){
    if (modeOn && item.selected) {
        binding.selected.rotation = -90F
        binding.selected.animate().rotation(0F).setDuration(400L).start()
        binding.selected.layoutParams.width = binding.selected.layoutParams.height
    } else
        binding.selected.layoutParams.width = 0

    binding.tvFileName.text = item.name
    binding.tvFileSize.text = item.size

    //  show the arrow in the video image
    binding.ivFileImagePlayArrow.visibility = if (item.type == MimeType.VIDEO) View.VISIBLE
    else View.GONE

    when (item.type) {
        MimeType.IMAGE -> {
            Glide
                .with(context)
                .load(item.uri)
                .placeholder(R.drawable.ic_default_image_file)
                .into(binding.ivFileImage)
        }

        MimeType.VIDEO -> {
            Glide
                .with(context)
                .load(item.uri)
                .placeholder(R.drawable.ic_default_video_file)
                .into(binding.ivFileImage)
        }

        MimeType.AUDIO -> {
            val albumCoverModelData = AlbumCoverDataModel(item.name, true)
            Glide.with(context)
                .load(albumCoverModelData)
                .placeholder(R.drawable.ic_default_audio_file)
                .into(binding.ivFileImage)
        }

        MimeType.PDF -> binding.ivFileImage.setImageResource(R.drawable.ic_default_pdf_file)

        MimeType.HTML -> binding.ivFileImage.setImageResource(R.drawable.ic_default_html_file)

        MimeType.TEXT -> binding.ivFileImage.setImageResource(R.drawable.ic_default_text_file)
        else -> {
        }
    }
}

fun getSpannableColoredString(context: Context, string: String, coloredSegment: String, colorRes: Int): SpannableString {

    if (!string.lowercase().contains(coloredSegment.lowercase()))
        return SpannableString(string)

    val ss = SpannableString(string)
    val color = ContextCompat.getColor(context, colorRes)
    val foregroundColor = ForegroundColorSpan(color)
    val start = string.lowercase().indexOf(coloredSegment)
    val end = start + coloredSegment.length
    ss.setSpan(foregroundColor, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    return ss
}
