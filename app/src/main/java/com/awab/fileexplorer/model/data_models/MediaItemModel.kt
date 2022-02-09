package com.awab.fileexplorer.model.data_models

import android.net.Uri
import com.awab.fileexplorer.model.types.MimeType

/**
 * a data class to represent a media file
 */
data class MediaItemModel(
    val name: String,
    val path: String,
    val size: String,
    val type: MimeType,
    val uri: Uri,
    var selected: Boolean = false
)