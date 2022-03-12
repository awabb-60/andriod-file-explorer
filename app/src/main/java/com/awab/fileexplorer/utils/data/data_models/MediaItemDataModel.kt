package com.awab.fileexplorer.utils.data.data_models

import android.net.Uri
import com.awab.fileexplorer.utils.data.types.MimeType

/**
 * a data class to represent a media file
 */
data class MediaItemDataModel(
    val name: String,
    val path: String,
    val size: String,
    val type: MimeType,
    val uri: Uri,
    var selected: Boolean = false
)