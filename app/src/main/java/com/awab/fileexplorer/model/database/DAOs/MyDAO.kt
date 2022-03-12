package com.awab.fileexplorer.model.database.DAOs

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.awab.fileexplorer.utils.data.data_models.PinedFileDataModel
import com.awab.fileexplorer.utils.data.data_models.RecentFileDataModel

@Dao
interface MyDAO {

    @Insert(entity = RecentFileDataModel::class, onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: RecentFileDataModel)

    @Insert(entity = PinedFileDataModel::class, onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: PinedFileDataModel)
}