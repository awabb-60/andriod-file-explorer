package com.awab.fileexplorer.model.data_models

import com.awab.fileexplorer.model.types.StorageType

data class StorageModel(
    val name:String,
    val size:String,
    val totalSizeBytes: Long,
    val freeSizeBytes: Long,
    val path:String,
    val storageType: StorageType
                         )
