package com.awab.fileexplorer.model.database.DAOs

import androidx.room.*
import com.awab.fileexplorer.utils.data.data_models.QuickAccessFileDataModel
import com.awab.fileexplorer.utils.data.types.QuickAccessFileType

@Dao
interface MyDAO {

    @Insert(entity = QuickAccessFileDataModel::class, onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(file: QuickAccessFileDataModel)

    @Delete(entity = QuickAccessFileDataModel::class)
    suspend fun deleteQuickAccessFile(file: QuickAccessFileDataModel)

    @Query("SELECT * FROM ${QuickAccessFileDataModel.tableName} WHERE type =:targetedType ORDER BY id")
    suspend fun getQuickAccessFiles(targetedType: QuickAccessFileType): List<QuickAccessFileDataModel>
}