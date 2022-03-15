package com.awab.fileexplorer.utils.data.data_models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = RecentFileDataModel.tableName)
data class RecentFileDataModel(
    val name: String,
    val path: String,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
) {
    companion object {
        const val tableName = "recent_files_table"
    }
}