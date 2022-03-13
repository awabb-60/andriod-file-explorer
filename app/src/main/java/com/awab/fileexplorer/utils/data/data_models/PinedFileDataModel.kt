package com.awab.fileexplorer.utils.data.data_models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = PinedFileDataModel.tableName)
data class PinedFileDataModel(
    val name: String,
    val path: String,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
) {
    companion object {
        const val tableName = "pined_files_table"
    }
}
