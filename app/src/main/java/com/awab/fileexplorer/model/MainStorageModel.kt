package com.awab.fileexplorer.model

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
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

    private val TAG = "AppDebug"

    private val database = DataBase.getInstance(context)
    private val dao = database.getDao()

    private val roomHandlerException = CoroutineExceptionHandler { _, exception ->
        Log.d(TAG, "exception handler ${exception.message}")
    }

    private lateinit var roomJob: Job

    private lateinit var queryFilesJob: Job
    private lateinit var loadingDetailsJob: Job
    private lateinit var deletingFilesJob: Job

    override fun saveTreeUri(treeUri: Uri, sdCardName: String) {
        val spE = context
            .getSharedPreferences(SD_CARD_TREE_URI_SP, AppCompatActivity.MODE_PRIVATE).edit()
        spE.putString(TREE_URI_ + sdCardName, treeUri.toString())
            .apply()
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

    override fun viewDarkModeSettings(): Boolean {
        val sp = context.getSharedPreferences(VIEW_SETTINGS_SHARED_PREFERENCES, AppCompatActivity.MODE_PRIVATE)
        return sp.getBoolean(SHARED_PREFERENCES_DARK_MODE_STATE, DEFAULT_DARK_MODE_STATE)
    }

    override fun saveViewingSettings(
        sortBy: String,
        order: String,
        showHiddenFiles: Boolean,
        darkModeState: Boolean
    ) {
        val sharedPreferencesEditor = context.getSharedPreferences(
            VIEW_SETTINGS_SHARED_PREFERENCES,
            AppCompatActivity.MODE_PRIVATE
        ).edit()

        sharedPreferencesEditor.putString(SHARED_PREFERENCES_SORTING_BY, sortBy)
            .putString(SHARED_PREFERENCES_SORTING_ORDER, order)
            .putBoolean(SHARED_PREFERENCES_SHOW_HIDDEN_FILES, showHiddenFiles)
            .putBoolean(SHARED_PREFERENCES_DARK_MODE_STATE, darkModeState)
            .apply()

    }

    override fun saveToQuickAccessFiles(list: List<QuickAccessFileDataModel>) {
        roomJob = CoroutineScope(Dispatchers.IO).launch {
            list.forEach {
                dao.insert(it)
            }
        }
    }

    override fun getQuickAccessFiles(
        targetedType: QuickAccessFileType,
        callback: SimpleSuccessAndFailureCallback<List<QuickAccessFileDataModel>>
    ) {
        roomJob = CoroutineScope(Dispatchers.IO).launch(roomHandlerException) {
            // filter the database first the getting the files
            val filteringJob = launch { filterQuickAccessFiles() }

            filteringJob.join()
            yield()
            dao.getQuickAccessFiles(targetedType).also {
                withContext(Dispatchers.Main) { callback.onSuccess(it) }
            }
        }

        roomJob.invokeOnCompletion { throwable ->
            if (throwable == null) {
                Log.d(TAG, "getting quick access job completed successfully")
            } else {
                callback.onFailure("error:can't load files")
            }
        }
    }

    /**
     * this will delete any none existing files (deleted files) that are still saved in the quick access files
     * @return true if the files has been filtered with no exceptions false otherwise
     */
    private suspend fun filterQuickAccessFiles() {
        val result: Deferred<List<QuickAccessFileDataModel>> = CoroutineScope(Dispatchers.IO).async {
            dao.getQuickAccessFiles()
        }
        result.await().forEach {
            if (!File(it.path).exists())
                deleteQuickAccessFile(it, object : SimpleSuccessAndFailureCallback<Boolean> {
                    override fun onSuccess(data: Boolean) {
                    }

                    override fun onFailure(message: String) {
                        throw Exception(message)
                    }
                })
        }
    }

    override fun deleteQuickAccessFile(
        file: QuickAccessFileDataModel,
        callback: SimpleSuccessAndFailureCallback<Boolean>?
    ) {
        val deleteJob = CoroutineScope(Dispatchers.IO).launch(roomHandlerException) {
            dao.deleteQuickAccessFile(file)
        }

        deleteJob.invokeOnCompletion { throwable ->
            if (throwable == null)
                callback?.onSuccess(true)
            else
                callback?.onFailure("cant load delete files")
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
        queryFilesJob = CoroutineScope(Dispatchers.Main).launch {
            val data = query(contentUri, projection, selection, selectionArgs)
            yield()
            if (data != null) {
                val filteredData = if (!viewHiddenFilesSettings())
                    data.filter { !it.name.startsWith('.') }
                else
                    data

                callback.onSuccess(filteredData)
            } else
                callback.onFailure("error cant query files")
        }
    }

    private suspend fun query(
        contentUri: Uri,
        projection: Array<String>? = arrayOf(MediaStore.MediaColumns.DATA),
        selection: String?,
        selectionArgs: Array<String>?,
    ): List<FileDataModel>? {
        return withContext(Dispatchers.Default) {

            val sortBy = when (viewSortBySettings()) {
                SORTING_BY_NAME -> MediaStore.MediaColumns.DISPLAY_NAME
                SORTING_BY_SIZE -> MediaStore.MediaColumns.SIZE
                SORTING_BY_DATE -> MediaStore.MediaColumns.DATE_MODIFIED
                else -> MediaStore.MediaColumns.DISPLAY_NAME
            }

            val order = when (viewSortOrderSettings()) {
                SORTING_ORDER_ASC -> "ASC"
                SORTING_ORDER_DEC -> "DESC"
                else -> "ASC"
            }

            Log.d(TAG, "query: \"$sortBy $order\"")

            try {
                val list = mutableListOf<FileDataModel>()
                // query the files
                val query = context.contentResolver.query(
                    contentUri, projection, selection, selectionArgs,
                    "$sortBy $order", null
                )

                // looping throw the query data
                query?.let {
                    it.use { cursor ->
                        val pathId = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                        for (i in 0..3) {
                            while (cursor.moveToNext() && isActive) {
                                val path = cursor.getString(pathId)
                                list.add(makeFileModel(File(path)))
                            }
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
        if (::queryFilesJob.isInitialized)
            queryFilesJob.cancel()
    }

    override fun getFilesDetails(
        list: List<FileDataModel>,
        callback: SimpleSuccessAndFailureCallback<FilesDetailsDataModel>
    ) {
        loadingDetailsJob = CoroutineScope(Dispatchers.Main).launch {

            val data = withContext(Dispatchers.Default) {
                FilesDetailsDataModel(getTotalSize(list), getContains(list, viewHiddenFilesSettings()))
            }

            // to check for if the job is still active
            yield()
            callback.onSuccess(data)
        }
    }

    override fun getSelectedMediaDetails(
        list: List<FileDataModel>,
        callback: SimpleSuccessAndFailureCallback<FilesDetailsDataModel>
    ) {
        loadingDetailsJob = CoroutineScope(Dispatchers.Main).launch {
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
        if (::loadingDetailsJob.isInitialized)
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

        deletingFilesJob = CoroutineScope(Dispatchers.Main).launch {
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
        deletingFilesJob = CoroutineScope(Dispatchers.Main).launch {
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