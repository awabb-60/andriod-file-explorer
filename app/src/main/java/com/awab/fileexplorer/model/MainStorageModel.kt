package com.awab.fileexplorer.model

import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.awab.fileexplorer.model.contrancts.StorageModel
import com.awab.fileexplorer.model.database.room.DataBase
import com.awab.fileexplorer.utils.*
import com.awab.fileexplorer.utils.callbacks.SimpleSuccessAndFailureCallback
import com.awab.fileexplorer.utils.data.data_models.FileDataModel
import com.awab.fileexplorer.utils.data.data_models.PinedFileDataModel
import com.awab.fileexplorer.utils.data.data_models.RecentFileDataModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * this the model class will load the date and save it
 * @param context the application context
 */
class MainStorageModel(val context: Context) : StorageModel {

    private val database = DataBase.getInstance(context)
    private val dao = database.getDao()

    override fun saveTreeUri(treeUri: Uri, sdCardName: String) {
        val spE = context
            .getSharedPreferences(SD_CARD_TREE_URI_SP, AppCompatActivity.MODE_PRIVATE).edit()
        spE.putString(TREE_URI_ + sdCardName, treeUri.toString())
        spE.apply()
    }

    override fun getTreeUri(storageName: String): Uri {
        val sp = context.getSharedPreferences(
            SD_CARD_TREE_URI_SP,
            AppCompatActivity.MODE_PRIVATE
        )
        return sp.getString(TREE_URI_ + storageName, "")!!.toUri()
    }

    override fun viewSortBySettings(): String? {
        val sp =
            context.getSharedPreferences(VIEW_SETTINGS_SHARED_PREFERENCES, AppCompatActivity.MODE_PRIVATE)
        return sp.getString(SHARED_PREFERENCES_SORTING_BY, DEFAULT_SORTING_ARGUMENT)
    }

    override fun viewSortOrderSettings(): String? {
        val sp =
            context.getSharedPreferences(VIEW_SETTINGS_SHARED_PREFERENCES, AppCompatActivity.MODE_PRIVATE)
        return sp.getString(SHARED_PREFERENCES_SORTING_ORDER, DEFAULT_SORTING_ORDER)
    }

    override fun viewHiddenFilesSettings(): Boolean {
        val sp =
            context.getSharedPreferences(VIEW_SETTINGS_SHARED_PREFERENCES, AppCompatActivity.MODE_PRIVATE)
        return sp.getBoolean(SHARED_PREFERENCES_SHOW_HIDDEN_FILES, DEFAULT_SHOW_HIDDEN_FILES)
    }

    override fun saveViewingSettings(sortBy: String, order: String, showHiddenFiles: Boolean) {
        val sharedPreferencesEditor = context.getSharedPreferences(
            VIEW_SETTINGS_SHARED_PREFERENCES,
            AppCompatActivity.MODE_PRIVATE
        ).edit()
        sharedPreferencesEditor.putString(SHARED_PREFERENCES_SORTING_BY, sortBy)
        sharedPreferencesEditor.putString(SHARED_PREFERENCES_SORTING_ORDER, order)
        sharedPreferencesEditor.putBoolean(SHARED_PREFERENCES_SHOW_HIDDEN_FILES, showHiddenFiles)
        sharedPreferencesEditor.apply()
    }

    override fun saveToRecentFiles(list: List<FileDataModel>) {
        MainScope().launch {
            list.forEach {
                val item = RecentFileDataModel(name = it.name, path = it.path)
                dao.insert(item)
            }
        }
    }

    override fun saveToPinedFiles(list: List<FileDataModel>) {
        MainScope().launch {
            list.forEach {
                val item = PinedFileDataModel(name = it.name, path = it.path)
                dao.insert(item)
            }
        }
    }

    override fun getPinedFiles(callback: SimpleSuccessAndFailureCallback<List<PinedFileDataModel>>) {
        MainScope().launch {
            dao.getPinedFiles().also {
                if (it.isEmpty())
                    callback.onFailure("no Pined Files")
                else
                    callback.onSuccess(it)
            }
        }
    }

    override fun getRecentFiles(callback: SimpleSuccessAndFailureCallback<List<RecentFileDataModel>>) {
        MainScope().launch {
            dao.getRecentFiles().also {
                if (it.isEmpty())
                    callback.onFailure("no Pined Files")
                else
                    callback.onSuccess(it)
            }
        }
    }
}
