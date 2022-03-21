package com.awab.fileexplorer.model

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import com.awab.fileexplorer.model.contrancts.StorageModel
import com.awab.fileexplorer.model.database.room.DataBase
import com.awab.fileexplorer.utils.*
import com.awab.fileexplorer.utils.callbacks.SimpleSuccessAndFailureCallback
import com.awab.fileexplorer.utils.data.data_models.FileDataModel
import com.awab.fileexplorer.utils.data.data_models.FilesDetailsDataModel
import com.awab.fileexplorer.utils.data.data_models.QuickAccessFileDataModel
import com.awab.fileexplorer.utils.data.types.QuickAccessFileType
import com.awab.fileexplorer.utils.data.types.StorageType
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
    private var loadingDetailsJob: CompletableJob = Job()
    private var deletingFilesJob: CompletableJob = Job()

    override fun saveTreeUri(treeUri: Uri, sdCardName: String) {
        val spE = context
            .getSharedPreferences(SD_CARD_TREE_URI_SP, AppCompatActivity.MODE_PRIVATE).edit()
        spE.putString(TREE_URI_ + sdCardName, treeUri.toString())
        spE.apply()
    }

    override fun getTreeUri(storageName: String): Uri? {
        val sp = context.getSharedPreferences(
            SD_CARD_TREE_URI_SP,
            AppCompatActivity.MODE_PRIVATE
        )
        val treeUriString = sp.getString(TREE_URI_ + storageName, "")
        return if (treeUriString != "")
            Uri.parse(treeUriString)
        else
            null

    }

    override fun viewSortBySettings(): String {
        val sp =
            context.getSharedPreferences(VIEW_SETTINGS_SHARED_PREFERENCES, AppCompatActivity.MODE_PRIVATE)
        return sp.getString(SHARED_PREFERENCES_SORTING_BY, DEFAULT_SORTING_ARGUMENT) ?: DEFAULT_SORTING_ARGUMENT
    }

    override fun viewSortOrderSettings(): String {
        val sp =
            context.getSharedPreferences(VIEW_SETTINGS_SHARED_PREFERENCES, AppCompatActivity.MODE_PRIVATE)
        return sp.getString(SHARED_PREFERENCES_SORTING_ORDER, DEFAULT_SORTING_ORDER) ?: DEFAULT_SORTING_ORDER
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
            dao.getQuickAccessFiles(targetedType).also { data ->
                withContext(Dispatchers.Main) { callback.onSuccess(data) }
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
            withContext(Dispatchers.Main) { callback?.onSuccess(true) }
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

                        while (cursor.moveToNext()) {
                            val path = cursor.getString(pathId)
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

    override fun getFilesDetails(
        list: List<FileDataModel>,
        callback: SimpleSuccessAndFailureCallback<FilesDetailsDataModel>
    ) {
        CoroutineScope(Dispatchers.Main + loadingDetailsJob).launch {

        val data = withContext(Dispatchers.Default) {
                FilesDetailsDataModel(getTotalSize(list), getContains(list, viewHiddenFilesSettings()))
            }
            callback.onSuccess(data)
        }
    }

    override fun getSelectedMediaDetails(
        list: List<FileDataModel>,
        callback: SimpleSuccessAndFailureCallback<FilesDetailsDataModel>
    ) {
        CoroutineScope(Dispatchers.Main + loadingDetailsJob).launch {
            val data = withContext(Dispatchers.Default) {
                // getting details for the selected items
                val contains = "${list.size} Files"
                var totalSizeBytes = 0L
                list.forEach {
                    totalSizeBytes += File(it.path).length()
                }
                val totalSize = getSize(totalSizeBytes)
                FilesDetailsDataModel(totalSize, contains)
            }
            callback.onSuccess(data)
        }
    }

    override fun cancelGetDetails() {
        loadingDetailsJob.cancel()
    }

    override fun deleteFromInternalStorage(
        list: List<FileDataModel>,
        storageType: StorageType,
        callback: SimpleSuccessAndFailureCallback<Boolean>
    ) {
        if (list.isEmpty()) {
            callback.onFailure("cant delete files")
            return
        }

        CoroutineScope(Dispatchers.Main + deletingFilesJob).launch {
            val success = deleteFilesFromInternalStorage(list)
            if (success)
                callback.onSuccess(success)
            else
                callback.onFailure("failed to delete some files")
        }
    }

    private suspend fun deleteFilesFromInternalStorage(list: List<FileDataModel>): Boolean {
        var allFilesDelete = true
        withContext(Dispatchers.Default) {
            try {
                for (fileModel in list) {
                    val file = File(fileModel.path)
                    // deleting the file if the file exists
                    if (file.exists()) {
                        val fileDeleted = file.deleteRecursively()

                        // if some files are not deleted
                        if (!fileDeleted)
                            allFilesDelete = false
                    }
                }
            } catch (e: Exception) {
                allFilesDelete = false
            }
        }
        return allFilesDelete
    }

    override fun deleteFromSdCard(
        list: List<FileDataModel>,
        parentFolder: DocumentFile,
        callback: SimpleSuccessAndFailureCallback<Boolean>
    ) {
        if (list.isEmpty()) {
            callback.onFailure("cant delete files")
            return
        }
        CoroutineScope(Dispatchers.Main + deletingFilesJob).launch {
            val success = deleteFilesFromSdCard(list, parentFolder)
            if (success)
                callback.onSuccess(success)
            else
                callback.onFailure("failed to delete some files")
        }
    }

    private suspend fun deleteFilesFromSdCard(list: List<FileDataModel>, parentFolder: DocumentFile): Boolean {
        var allFilesDeleted = true

        withContext(Dispatchers.Default) {
            //  deleting all the targeted files
            list.forEach {
                try {
                    val fileDeleted = parentFolder.findFile(it.name)?.delete()
                    // some error occur... the file was not found or couldn't delete this file
                    if (fileDeleted == null || !fileDeleted) {
                        allFilesDeleted = false
                    }
                } catch (e: Exception) {
                    allFilesDeleted = false
                }
            }
        }
        return allFilesDeleted
    }
}