package com.awab.fileexplorer.model

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.awab.fileexplorer.model.contrancts.StorageModel
import com.awab.fileexplorer.model.database.room.DataBase
import com.awab.fileexplorer.model.utils.makeFileModel
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

    private var queryFilesJob: CompletableJob = Job()

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

    override fun queryFiles(
        contentUri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        callback: SimpleSuccessAndFailureCallback<List<FileDataModel>>
    ) {
        // running coroutine in the main scope
        CoroutineScope(Dispatchers.Main + queryFilesJob).launch {
            val data = query(contentUri, projection, selection, selectionArgs)
            if (data != null) {
                val filteredData = if (!viewHiddenFilesSettings())
                    data.filter { !it.name.startsWith('.') }
                else
                    data

                callback.onSuccess(filteredData)
            } else
                callback.onFailure("error cant load media files")
        }
    }

    private suspend fun query(
        contentUri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
    ): List<FileDataModel>? {
        return withContext(Dispatchers.Default) {
            try {
                val list = mutableListOf<FileDataModel>()
                // query the files
                val query = context.contentResolver.query(
                    contentUri, projection, selection, selectionArgs,
                    "${MediaStore.MediaColumns.DATE_MODIFIED} DESC", null
                )

                // looping throw the query data
                query?.let {
                    it.use { cursor ->
                        val pathId = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                        val path2Id = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH)
                        while (cursor.moveToNext()) {
                            val path = cursor.getString(pathId)
                            val path1 = cursor.getString(path2Id)
                            list.add(makeFileModel(File(path)))
                        }
                    }
                }
                query?.close()
                list
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun cancelQueryFiles() {
        queryFilesJob.cancel()
    }

}