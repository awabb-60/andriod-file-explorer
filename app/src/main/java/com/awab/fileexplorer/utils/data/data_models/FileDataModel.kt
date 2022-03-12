package com.awab.fileexplorer.utils.data.data_models

import android.net.Uri
import com.awab.fileexplorer.utils.data.types.FileType
import com.awab.fileexplorer.utils.data.types.MimeType
import java.util.*

/**
 * a data class to represent a file in the storage
 */
data class FileDataModel(
    val name: String,
    val path: String,
    val size:String,
    val date: Date,
    val type: FileType,
    val mimeType: MimeType,
    val uri: Uri,
    val isEmpty:Boolean = true,
    var selected:Boolean = false
)
