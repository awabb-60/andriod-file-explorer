package com.awab.fileexplorer.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ImageView
import android.widget.ImageView.ScaleType.CENTER_CROP
import android.widget.ImageView.ScaleType.CENTER_INSIDE
import androidx.core.content.ContextCompat
import com.awab.fileexplorer.R
import com.awab.fileexplorer.databinding.FileItemBinding
import com.awab.fileexplorer.databinding.PinedFileLayoutBinding
import com.awab.fileexplorer.utils.data.data_models.AlbumCoverDataModel
import com.awab.fileexplorer.utils.data.data_models.AppIconDataModel
import com.awab.fileexplorer.utils.data.data_models.FileDataModel
import com.awab.fileexplorer.utils.data.types.FileType
import com.awab.fileexplorer.utils.data.types.MimeType
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import java.io.File

fun bindFileItem(context: Context, item: FileDataModel, binding: FileItemBinding, modeOn: Boolean) {
    if (modeOn && item.selected) {
        binding.selected.rotation = -90F
        binding.selected.animate().rotation(0F).setDuration(400L).start()
        binding.selected.layoutParams.width = binding.selected.layoutParams.height
    } else
        binding.selected.layoutParams.width = 0

    binding.tvFileName.text = item.name

    if (item.type == FileType.FILE) {
        binding.tvFileSize.text = item.size
    } else if (item.type == FileType.DIRECTORY) {
        binding.tvFileSize.text = ""
    }
    loadFileImage(context, item, binding.ivFileImage)

    //  show the arrow in the video thumbnail
    binding.ivFileImagePlayArrow.visibility = if (item.mimeType == MimeType.VIDEO) View.VISIBLE
    else View.GONE
}

fun bindFileItem(context: Context, item: FileDataModel, binding: PinedFileLayoutBinding) {
    binding.tvPinedFileName.text = item.name
    loadFileImage(context, item, binding.tvPinedFileImage)
}

private fun loadFileImage(context: Context, item: FileDataModel, view: ImageView) {
    if (item.type == FileType.FILE) {
        // setting the image uri or the default file image
        when (item.mimeType) {
            MimeType.IMAGE -> {
                Glide.with(context)
                    .load(item.uri)
                    .addListener(ScaleTypeRequestListener(view, CENTER_INSIDE, CENTER_CROP))
                    .placeholder(R.drawable.ic_default_image_file)
                    .into(view)
            }

            MimeType.VIDEO -> {
                Glide.with(context)
                    .load(item.uri)
                    .addListener(ScaleTypeRequestListener(view, CENTER_INSIDE, CENTER_CROP))
                    .placeholder(R.drawable.ic_default_video_file)
                    .into(view)
            }

            MimeType.APPLICATION -> {
                val appIconModelData = AppIconDataModel(item.path, File(item.path).isFile)
                Glide.with(context)
                    .load(appIconModelData)
                    .placeholder(R.drawable.ic_default_application_file)
                    .into(view)
            }

            MimeType.AUDIO -> {
                val albumCoverModelData = AlbumCoverDataModel(item.name, File(item.path).isFile)
                Glide.with(context)
                    .load(albumCoverModelData)
                    .addListener(ScaleTypeRequestListener(view, CENTER_INSIDE, CENTER_CROP))
                    .placeholder(R.drawable.ic_default_audio_file)
                    .into(view)
            }

            MimeType.TEXT -> {
                view.setImageResource(R.drawable.ic_default_text_file)
                view.scaleType = CENTER_INSIDE
            }

            MimeType.HTML -> {
                view.setImageResource(R.drawable.ic_default_html_file)
                view.scaleType = CENTER_INSIDE
            }

            MimeType.PDF -> {
                view.setImageResource(R.drawable.ic_default_pdf_file)
                view.scaleType = CENTER_INSIDE
            }

            MimeType.UNKNOWN -> {
                view.setImageResource(R.drawable.ic_default_file)
                view.scaleType = CENTER_INSIDE
            }
        }
    } else if (item.type == FileType.DIRECTORY) {
        val resource = if (item.isEmpty) R.drawable.ic_empty_folder else R.drawable.ic_folder
        view.setImageResource(resource)
        view.scaleType = CENTER_INSIDE
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

/**
 * callback class to set the required image scale type
 * after glide loading
 */
class ScaleTypeRequestListener(
    val view: ImageView,
    private val onFlierScaleType: ImageView.ScaleType,
    private val onSuccessScaleType: ImageView.ScaleType
) : RequestListener<Drawable> {
    override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<Drawable>?,
        isFirstResource: Boolean
    ): Boolean {
        view.scaleType = onFlierScaleType
        return false
    }

    override fun onResourceReady(
        resource: Drawable?,
        model: Any?,
        target: Target<Drawable>?,
        dataSource: DataSource?,
        isFirstResource: Boolean
    ): Boolean {
        view.scaleType = onSuccessScaleType
        return false
    }
}