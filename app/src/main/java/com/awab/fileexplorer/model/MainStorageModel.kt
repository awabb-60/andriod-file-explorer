package com.awab.fileexplorer.model

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.awab.fileexplorer.model.contrancts.StorageModel
import com.awab.fileexplorer.model.database.room.DataBase
import com.awab.fileexplorer.model.utils.makeFileModels
import com.awab.fileexplorer.utils.*
import com.awab.fileexplorer.utils.callbacks.SimpleSuccessAndFailureCallback
import com.awab.fileexplorer.utils.data.data_models.FileDataModel
import com.awab.fileexplorer.utils.data.data_models.QuickAccessFileDataModel
import com.awab.fileexplorer.utils.data.types.QuickAccessFileType
import kotlinx.coroutines.*
import java.io.File

/**
 * this the model class will load the date and save it
 * @param context the application context
 */
class MainStorageModel(val context: Context) : StorageModel {

    private val database = DataBase.getInstance(context)
    private val dao = database.getDao()

    private val coroutineIOScope = CoroutineScope(Dispatchers.IO)
    private val coroutineMainScope = CoroutineScope(Dispatchers.Main.immediate)

    private var searchListJob: Job? = null

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

    override fun saveToQuickAccessFiles(list: List<QuickAccessFileDataModel>) {
        coroutineIOScope.launch {
            list.forEach {
                dao.insert(it)
            }
        }
    }

    override fun getQuickAccessFiles(
        targetedType: QuickAccessFileType,
        callback: SimpleSuccessAndFailureCallback<List<QuickAccessFileDataModel>>
    ) {
        coroutineIOScope.launch {
            // filtering the deleted files
            filterQuickAccessFiles()
            dao.getQuickAccessFiles(targetedType).also {
                withContext(Dispatchers.Main.immediate) {
                    if (it.isEmpty())
                        callback.onFailure("no Quick Access Files")
                    else
                        callback.onSuccess(it)
                }
            }
        }
    }

    /**
     * this will delete any no existing files (deleted files) that are still saved in the quick access files
     */
    private suspend fun filterQuickAccessFiles() {
        val noExistingFiles = mutableListOf<QuickAccessFileDataModel>()
        dao.getQuickAccessFiles(QuickAccessFileType.PINED).forEach {
            if (!File(it.path).exists())
                noExistingFiles.add(it)
        }

        dao.getQuickAccessFiles(QuickAccessFileType.RECENT).forEach {
            if (!File(it.path).exists())
                noExistingFiles.add(it)
        }

        noExistingFiles.forEach {
            dao.deleteQuickAccessFile(it)
        }
    }

    override fun deleteQuickAccessFile(
        file: QuickAccessFileDataModel,
        callback: SimpleSuccessAndFailureCallback<Boolean>?
    ) {
        coroutineIOScope.launch {
            dao.deleteQuickAccessFile(file)
            callback?.onSuccess(true)
        }
    }

    override fun loadSearchList(
        folderPath: String,
        callback: SimpleSuccessAndFailureCallback<List<FileDataModel>>
    ) {
        searchListJob = coroutineMainScope.launch {
            // loading the files in suspend fun running on the background thread
            val data = getSearchList(folderPath)

            if (data != null)
                callback.onSuccess(data)
            else
                callback.onFailure("error happened while loading the files")
        }
    }

    override fun cancelLoadSearchList() {
        // cancel the job if started
        searchListJob?.cancel()
    }

    private suspend fun getSearchList(folderPath: String): List<FileDataModel>? {
        return withContext(Dispatchers.Default) {
            try {
                // the query for all the files that start with the folderPath
                val query = context.contentResolver.query(
                    MediaStore.Files.getContentUri("external"),
                    null, "_data Like ?",
                    arrayOf("%$folderPath%"), null
                )
                val allFiles = mutableListOf<File>()
                // getting the files paths
                query?.let { cursor ->
                    cursor.use {
                        val pathId = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                        while (it.moveToNext()) {
                            val path = it.getString(pathId)
                            allFiles.add(File(path))
                        }
                    }
                }

                // making the file models
                makeFileModels(allFiles)
            } catch (e: Exception) {
                // error while loading the files
                null
            }
        }
    }
}