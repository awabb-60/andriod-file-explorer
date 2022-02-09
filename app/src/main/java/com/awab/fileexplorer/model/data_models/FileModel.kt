package com.awab.fileexplorer.model.data_models

import android.net.Uri
import com.awab.fileexplorer.model.types.FileType
import com.awab.fileexplorer.model.types.MimeType
import java.util.*

/**
 * a data class to represent a file in the storage
 */
data class FileModel(
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
