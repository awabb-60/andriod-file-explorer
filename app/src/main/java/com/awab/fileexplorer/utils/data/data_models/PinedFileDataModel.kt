package com.awab.fileexplorer.utils.data.data_models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pined_files_table")
data class PinedFileDataModel(
    val name: String,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)
