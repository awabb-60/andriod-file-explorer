package com.awab.fileexplorer.model.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.awab.fileexplorer.model.database.DAOs.MyDAO
import com.awab.fileexplorer.utils.data.data_models.PinedFileDataModel
import com.awab.fileexplorer.utils.data.data_models.RecentFileDataModel

@Database(entities = [RecentFileDataModel::class, PinedFileDataModel::class], version = 1, exportSchema = false)
abstract class DataBase : RoomDatabase() {

    abstract fun getDao(): MyDAO

    companion object {
        /**
         * a singleton instance of this database
         */
        @Volatile
        private var INSTANCE: DataBase? = null

        /**
         *  returns the singleton instance of this database
         *  @return room database of type HistoryDateBase
         */
        fun getInstance(context: Context): DataBase {
            synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        DataBase::class.java,
                        "pined_and_recent_files_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }
                return INSTANCE as DataBase
            }
        }


    }
}