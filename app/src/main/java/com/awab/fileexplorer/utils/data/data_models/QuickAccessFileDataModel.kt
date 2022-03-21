package com.awab.fileexplorer.utils.data.data_models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.awab.fileexplorer.utils.data.types.QuickAccessFileType


@Entity(tableName = QuickAccessFileDataModel.tableName)
data class QuickAccessFileDataModel(
    val name: String,
    val path: String,
    val type: QuickAccessFileType,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
) {
    companion object {
        const val tableName = "quick_access_file"
    }
}
